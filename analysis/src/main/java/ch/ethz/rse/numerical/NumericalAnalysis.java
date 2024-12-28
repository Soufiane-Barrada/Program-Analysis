package ch.ethz.rse.numerical;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Manager;
import apron.MpqScalar;
import apron.Polka;
import apron.Tcons1;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.pointer.StoreInitializer;
import ch.ethz.rse.pointer.PointsToInitializer;
import ch.ethz.rse.utils.Constants;
import ch.ethz.rse.verify.EnvironmentGenerator;
import soot.ArrayType;
import soot.DoubleType;
import soot.Local;
import soot.RefType;
import soot.SootHelper;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.MulExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;

/**
 * Convenience class running a numerical analysis on a given {@link SootMethod}
 */
public class NumericalAnalysis extends ForwardBranchedFlowAnalysis<NumericalStateWrapper> {

	private static final Logger logger = LoggerFactory.getLogger(NumericalAnalysis.class);

	private final SootMethod method;

	/**
	 * the property we are verifying
	 */
	private final VerificationProperty property;

	/**
	 * the pointer analysis result we are verifying
	 */
	private final PointsToInitializer pointsTo;

	/**
	 * all store initializers encountered until now
	 */
	private Set<StoreInitializer> alreadyInit;

	/**
	 * number of times this loop head was encountered during analysis
	 */
	private HashMap<Unit, IntegerWrapper> loopHeads = new HashMap<Unit, IntegerWrapper>();
	/**
	 * Previously seen abstract state for each loop head
	 */
	private HashMap<Unit, NumericalStateWrapper> loopHeadState = new HashMap<Unit, NumericalStateWrapper>();
	//added to keep track of the other stream as well for widening
	private HashMap<Unit, NumericalStateWrapper> loopHeadState1 = new HashMap<Unit, NumericalStateWrapper>(); 

	/**
	 * Numerical abstract domain to use for analysis: Convex polyhedra
	 */
	public final Manager man = new Polka(true);

	public final Environment env;

	/**
	 * We apply widening after updating the state at a given merge point for the
	 * {@link WIDENING_THRESHOLD}th time
	 */
	private static final int WIDENING_THRESHOLD = 6;

	/**
	 * 
	 * @param method   method to analyze
	 * @param property the property we are verifying
	 */
	public NumericalAnalysis(SootMethod method, VerificationProperty property, PointsToInitializer pointsTo) {
		super(SootHelper.getUnitGraph(method));

		UnitGraph g = SootHelper.getUnitGraph(method);

		this.property = property;

		this.pointsTo = pointsTo;
		
		this.method = method;

		this.alreadyInit = new HashSet<StoreInitializer>();

		this.env = new EnvironmentGenerator(method, pointsTo).getEnvironment();

		// initialize counts for loop heads
		for (Loop l : new LoopNestTree(g.getBody())) {
			loopHeads.put(l.getHead(), new IntegerWrapper(0));
		}

		// perform analysis by calling into super-class
		logger.info("Analyzing {} in {}", method.getName(), method.getDeclaringClass().getName());
		doAnalysis(); // calls newInitialFlow, entryInitialFlow, merge, flowThrough, and stops when a fixed point is reached
	}

	/**
	 * Report unhandled instructions, types, cases, etc.
	 * 
	 * @param task description of current task
	 * @param what
	 */
	public static void unhandled(String task, Object what, boolean raiseException) {
		String description = task + ": Can't handle " + what.toString() + " of type " + what.getClass().getName();

		if (raiseException) {
			logger.error("Raising exception " + description);
			throw new UnsupportedOperationException(description);
		} else {
			logger.error(description);

			// print stack trace
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (int i = 1; i < stackTrace.length; i++) {
				logger.error(stackTrace[i].toString());
			}
		}
	}

	@Override
	protected void copy(NumericalStateWrapper source, NumericalStateWrapper dest) {
		source.copyInto(dest);
	}

	@Override
	protected NumericalStateWrapper newInitialFlow() {
		// should be bottom (only entry flows are not bottom originally)
		return NumericalStateWrapper.bottom(man, env);
	}

	@Override
	protected NumericalStateWrapper entryInitialFlow() {
		// state of entry points into function
		NumericalStateWrapper ret = NumericalStateWrapper.top(man, env);

		// TODO: MAYBE FILL THIS OUT
		if (this.property == VerificationProperty.FITS_IN_RESERVE){
			// Initialize received_amount with 0
			Texpr1Intern cstExpr = createTexpr1Intern(createTexpr1NodeFromInt(0));
			for(StoreInitializer initializer : this.pointsTo.getInitializers(this.method)) {
				ret.assign(initializer.getUniqueLabel(), cstExpr);
			}
		}

		return ret;
	}

	@Override
	protected void merge(Unit succNode, NumericalStateWrapper w1, NumericalStateWrapper w2, NumericalStateWrapper w3) {
		// merge the two states from w1 and w2 and store the result into w3
		logger.debug("in merge: " + succNode);

		// TODO: CHECK
		w1.merge(w2, w3);
	}

	@Override
	protected void merge(NumericalStateWrapper src1, NumericalStateWrapper src2, NumericalStateWrapper trg) {
		// this method is never called, we are using the other merge instead
		throw new UnsupportedOperationException();
	}

	@Override
	protected void flowThrough(NumericalStateWrapper inWrapper, Unit op, List<NumericalStateWrapper> fallOutWrappers,
			List<NumericalStateWrapper> branchOutWrappers) {
		logger.debug(inWrapper + " " + op + " => ?");

		Stmt s = (Stmt) op;

		// fallOutWrapper is the wrapper for the state after running op,
		// assuming we move to the next statement. Do not overwrite
		// fallOutWrapper, but use its .set method instead
		assert fallOutWrappers.size() <= 1;
		NumericalStateWrapper fallOutWrapper = null;
		if (fallOutWrappers.size() == 1) {
			fallOutWrapper = fallOutWrappers.get(0);
			inWrapper.copyInto(fallOutWrapper);
		}

		// branchOutWrapper is the wrapper for the state after running op,
		// assuming we follow a conditional jump. It is therefore only relevant
		// if op is a conditional jump. In this case, (i) fallOutWrapper
		// contains the state after "falling out" of the statement, i.e., if the
		// condition is false, and (ii) branchOutWrapper contains the state
		// after "branching out" of the statement, i.e., if the condition is
		// true.
		assert branchOutWrappers.size() <= 1;
		NumericalStateWrapper branchOutWrapper = null;
		if (branchOutWrappers.size() == 1) {
			branchOutWrapper = branchOutWrappers.get(0);
			inWrapper.copyInto(branchOutWrapper);
		}

		try {
			if (s instanceof DefinitionStmt) {
				// handle assignment

				DefinitionStmt sd = (DefinitionStmt) s;
				Value left = sd.getLeftOp();
				Value right = sd.getRightOp();

				// We are not handling these cases:
				if (!(left instanceof JimpleLocal)) {
					unhandled("Assignment to non-local variable", left, true);
				} else if (left instanceof JArrayRef) {
					unhandled("Assignment to a non-local array variable", left, true);
				} else if (left.getType() instanceof ArrayType) {
					unhandled("Assignment to Array", left, true);
				} else if (left.getType() instanceof DoubleType) {
					unhandled("Assignment to double", left, true);
				} else if (left instanceof JInstanceFieldRef) {
					unhandled("Assignment to field", left, true);
				}

				if (left.getType() instanceof RefType) {
					// assignments to references are handled by pointer analysis
					// no action necessary
				} else {
					// handle assignment
					handleDef(fallOutWrapper, left, right);
				}

			} else if (s instanceof JIfStmt) {
				// handle if
				// TODO: CHECK
				logger.debug("If statement: {}", s);
				JIfStmt ifStmt = (JIfStmt) s;
				handleIf(ifStmt, fallOutWrapper, branchOutWrapper);
				// keep track of loop heads and apply widening if necessary
				if (loopHeads.keySet().contains(s)) {
					NumericalStateWrapper loopState = loopHeadState.get(s);
					NumericalStateWrapper loopState1 = loopHeadState1.get(s);
					if (loopState != null) {
						int loopCount = loopHeads.get(s).value + 1;
						if (loopCount > WIDENING_THRESHOLD){
							branchOutWrapper.widening(loopState); // apply widening
							fallOutWrapper.widening(loopState1); // apply widening
							logger.debug("Widening applied: {}", branchOutWrapper);
							logger.debug("Widening applied: {}", fallOutWrapper);
							loopHeads.put(s, new IntegerWrapper(0)); // reset loop counter after widening
						}else{
							loopHeads.put(s, new IntegerWrapper(loopCount)); // increment loop counter
						}
					}
					//update the loop head states for both streams
					loopHeadState.put(s, branchOutWrapper.copy());
					loopHeadState1.put(s, fallOutWrapper.copy());
				}

			} else if (s instanceof JInvokeStmt) {
				// handle invocations
				JInvokeStmt jInvStmt = (JInvokeStmt) s;
				InvokeExpr invokeExpr = jInvStmt.getInvokeExpr();
				if (invokeExpr instanceof JVirtualInvokeExpr) {
					handleInvoke(jInvStmt, fallOutWrapper);
				} else if (invokeExpr instanceof JSpecialInvokeExpr) {
					// initializer for object
					handleInitialize(jInvStmt, fallOutWrapper);
				} else {
					unhandled("Unhandled invoke statement", invokeExpr, true);
				}
			} else if (s instanceof JGotoStmt) {
				// safe to ignore
			} else if (s instanceof JReturnVoidStmt) {
				// safe to ignore
			} else {
				unhandled("Unhandled statement", s, true);
			}

			// log outcome
			if (fallOutWrapper != null) {
				logger.debug(inWrapper.get() + " " + s + " =>[fallout] " + fallOutWrapper);
			}
			if (branchOutWrapper != null) {
				logger.debug(inWrapper.get() + " " + s + " =>[branchout] " + branchOutWrapper);
			}

		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	public void handleInvoke(JInvokeStmt jInvStmt, NumericalStateWrapper fallOutWrapper) throws ApronException {
		// TODO: MAYBE FILL THIS OUT
		if (this.property == VerificationProperty.FITS_IN_RESERVE) {
			// TODO: CHECK
			// Only necessary for the fits_in_reserve property. 
			// Just to update the variable for the received amount.
			InvokeExpr invokeExpr = jInvStmt.getInvokeExpr();
			SootMethod invokedMethod = invokeExpr.getMethod();
			String methodName = invokedMethod.getName();
			Local baseName = (Local) ((JVirtualInvokeExpr) invokeExpr).getBase();
		
			if (methodName.equals(Constants.getDeliveryFunctionName) && baseName.getType().toString().equals(Constants.StoreClassName)) {
				//Only for the get_delivery method on the Store class
				if (invokedMethod.getParameterCount() != 1) {
					logger.debug("The get_delivery method should have 1 integer parameter.");
				}
				logger.debug("Handling invocation of method " + methodName + " on object " + baseName);
				List<StoreInitializer> initializers = this.pointsTo.pointsTo(baseName);
				if (initializers.size() == 0) {
					logger.debug("There are no initializer.");
				}
				logger.debug("number of initializers: " + this.pointsTo.pointsTo(baseName).size());
				for (StoreInitializer initializer : this.pointsTo.pointsTo(baseName)) {
					// If there are multiple initializers, the received amount incremented for each...
					if (alreadyInit.contains(initializer)){
						// Increment the received amount if the store has already been initialized.
						logger.debug(methodName + " on object " + baseName + " points to " + initializer);
						String storeName = initializer.getUniqueLabel();
						Texpr1Node argNode = createTexpr1NodeFromValue(invokeExpr.getArg(0));
						Texpr1Node receivedNode = new Texpr1VarNode(storeName);
						Texpr1Intern sumExpr = createTexpr1Intern(new Texpr1BinNode(Texpr1BinNode.OP_ADD, argNode, receivedNode));
						fallOutWrapper.assign(storeName, sumExpr);
					} else {
						logger.debug("Invocation of method " + methodName + " on object " + baseName + " before initialization.");
					}
				}
			} else {
				// Other method invocations not handled
				logger.debug("Ignoring invocation of method " + methodName);
			}
		}
	}

	public void handleInitialize(JInvokeStmt jInvStmt, NumericalStateWrapper fallOutWrapper) throws ApronException {
		// TODO: CHECK
		// Only necessary for the fits_in_reserve property. 
		if (this.property == VerificationProperty.FITS_IN_RESERVE) {
			InvokeExpr invokeExpr = jInvStmt.getInvokeExpr();
			// Check if the initializer is relevant i.e. if it is an initializer of a Store.
			if(this.pointsTo.isRelevantInit((JSpecialInvokeExpr)invokeExpr)) {
				List<Value> args = invokeExpr.getArgs();
				// Check if the arguments are consistent with the expected format.
				if (args.size() == 2 && args.get(0) instanceof IntConstant && args.get(1) instanceof IntConstant) {
					Local baseName = (Local) ((JSpecialInvokeExpr)invokeExpr).getBase();
					logger.debug("number of initializers: " + this.pointsTo.pointsTo(baseName).size());
					for (StoreInitializer initializer : this.pointsTo.pointsTo(baseName)) {
						this.alreadyInit.add(initializer); // add to already initialized stores
					}
				} else {
					logger.debug("Arguments of store initialization is not consistent with the expected format.");
				}
			}else {
				logger.debug("Ignoring initialization of object " + invokeExpr.getMethod().getDeclaringClass().getName());
			}
		}
	}

	// returns state of in after assignment
	private void handleDef(NumericalStateWrapper fallOutWrapper, Value left, Value right) throws ApronException {
		// TODO: CHECK
		// The left type is controlled by the main body above.
		Texpr1Intern exprIntern;
		String var = fallOutWrapper.getVarName(left);
		if (right instanceof IntConstant) {
			exprIntern = createTexpr1Intern(createTexpr1NodeFromValue((IntConstant) right));
			fallOutWrapper.assign(var, exprIntern);
		} else if (right instanceof BinopExpr) {
			exprIntern = createTexpr1Intern(createTexpr1NodeFromBinop((BinopExpr) right));
			fallOutWrapper.assign(var, exprIntern);
		} else if (right instanceof Local){
			exprIntern = createTexpr1Intern(createTexpr1NodeFromLocal((Local) right));
			fallOutWrapper.assign(var, exprIntern);
		}else {
			// Hopefully above covers all cases required...
			unhandled("Unhandled def right type", right, false);
		}	 
	}

	/**
	 * Handle if statement
	 */
	private void handleIf(JIfStmt ifStmt, NumericalStateWrapper fallOutWrapper, NumericalStateWrapper branchOutWrapper) throws ApronException {
		ConditionExpr condition = (ConditionExpr) ifStmt.getCondition();
		Texpr1Node leftNode = createTexpr1NodeFromValue(condition.getOp1());
		Texpr1Node rightNode = createTexpr1NodeFromValue(condition.getOp2());
		Texpr1Node diffNode = new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode);
		Texpr1Node diffNodeNeg = new Texpr1BinNode(Texpr1BinNode.OP_SUB, rightNode, leftNode);
		int constraintType;
		int negatedConstraintType;
		
		if (condition instanceof JEqExpr) {
			constraintType = Tcons1.EQ;
			negatedConstraintType = Tcons1.DISEQ;
		} else if (condition instanceof JNeExpr) {
			constraintType = Tcons1.DISEQ;
			negatedConstraintType = Tcons1.EQ;
		} else if (condition instanceof JGtExpr) {
			constraintType = Tcons1.SUP;
			negatedConstraintType = Tcons1.SUPEQ;
		} else if (condition instanceof JGeExpr) {
			constraintType = Tcons1.SUPEQ;
			negatedConstraintType = Tcons1.SUP;
		} else if (condition instanceof JLtExpr) {
			constraintType = Tcons1.SUP;
			negatedConstraintType = Tcons1.SUPEQ;
			Texpr1Node tmp = diffNode;
			diffNode = diffNodeNeg;
			diffNodeNeg = tmp;
		} else if (condition instanceof JLeExpr) {
			constraintType = Tcons1.SUPEQ;
			negatedConstraintType = Tcons1.SUP;
			Texpr1Node tmp = diffNode;
			diffNode = diffNodeNeg;
			diffNodeNeg = tmp;
		} else {
			// Should be only the above cases
			unhandled("Unspported condition: ", condition, true);
			return;
		}

		// Create the constraints based on the nodes.
		Texpr1Intern diffExpr = createTexpr1Intern(diffNode);
		Texpr1Intern diffExprNeg = createTexpr1Intern(diffNodeNeg);
		
		// Handle fall-out path i.e. path for skipping if part
		fallOutWrapper.meet(new Tcons1(negatedConstraintType, diffExprNeg));
	
		// Handle branch-out path i.e. path for executing if part
		branchOutWrapper.meet(new Tcons1(constraintType, diffExpr));
	}

	/**
	 * Handles a binary operation expression.
	 */
	private Texpr1Node createTexpr1NodeFromBinop(BinopExpr binop) {
		Texpr1Node leftNode = createTexpr1NodeFromValue(binop.getOp1());
		Texpr1Node rightNode = createTexpr1NodeFromValue(binop.getOp2());
	
		if (binop instanceof AddExpr) {
			return new Texpr1BinNode(Texpr1BinNode.OP_ADD, leftNode, rightNode);
		} else if (binop instanceof SubExpr) {
			return new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode);
		} else if (binop instanceof MulExpr) {
			return new Texpr1BinNode(Texpr1BinNode.OP_MUL, leftNode, rightNode);
		} else {
			// Should be only the above cases 
			unhandled("Binop Operation not handled", binop, true);
			return null;
		}
	}
	
	/**
	 * Creates a Texpr1Node from a Value
	 */
	private Texpr1Node createTexpr1NodeFromValue(Value value){
		if (value instanceof IntConstant) {
			return new Texpr1CstNode(new MpqScalar(((IntConstant) value).value));
		} else if (value instanceof Local) {
			return new Texpr1VarNode(((Local) value).getName());
		} else {
			// Any other cases..?
			unhandled("Unhandled value type", value, true);
			return null;
		}
	}

	/**
	 * Creates a Texpr1Node from an int
	 */
	private Texpr1Node createTexpr1NodeFromInt(int value){
		return new Texpr1CstNode(new MpqScalar(value));
	}

	/**
	 * Creates a Texpr1Node from a local
	 */
	private Texpr1Node createTexpr1NodeFromLocal(Local local){
		return new Texpr1VarNode(local.getName());
	}

	/**
	 * Creates a Texpr1Intern from a node
	 */
	private Texpr1Intern createTexpr1Intern(Texpr1Node node){
		return new Texpr1Intern(this.env, node);
	}
	
}
