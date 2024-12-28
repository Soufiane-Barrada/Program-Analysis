package ch.ethz.rse.verify;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.MpqScalar;
import apron.Texpr1CstNode;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import ch.ethz.rse.VerificationProperty;
import ch.ethz.rse.numerical.NumericalAnalysis;
import ch.ethz.rse.numerical.NumericalStateWrapper;
import ch.ethz.rse.pointer.StoreInitializer;
import ch.ethz.rse.pointer.PointsToInitializer;
import ch.ethz.rse.utils.Constants;
import polyglot.ast.Call;
import soot.Local;
import soot.SootClass;
import soot.SootHelper;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.UnitGraph;

/**
 * Main class handling verification
 * 
 */
public class Verifier extends AVerifier {

	private static final Logger logger = LoggerFactory.getLogger(Verifier.class);

	/**
	 * class to be verified
	 */
	private final SootClass c;

	/**
	 * points to analysis for verified class
	 */
	private final PointsToInitializer pointsTo;

	/**
	 * 
	 * @param c class to verify
	 */
	public Verifier(SootClass c) {
		logger.debug("Analyzing {}", c.getName());

		this.c = c;

		// pointer analysis
		this.pointsTo = new PointsToInitializer(this.c);
	}


	protected void runNumericalAnalysis(VerificationProperty property) {
		// TODO: CHECK
		for (SootMethod method : this.c.getMethods()) {
			NumericalAnalysis na = new NumericalAnalysis(method, property, this.pointsTo);
			this.numericalAnalysis.put(method, na);
		}
	}

	@Override
	public boolean checksNonNegative() {
		// TODO: CHECK
		for (SootMethod method : this.c.getMethods()) {
			for (CallToDelivery callToDelivery : findCallToGetDeliveries(method)) {
				logger.debug("Checking non-negative volume for call to delivery");
				NumericalStateWrapper stateBefore = callToDelivery.getStateBefore();
				// Check if state is not bottom
				if (!stateBefore.isBottom()){
					JVirtualInvokeExpr invokeExpr = callToDelivery.getInvokeExpr();
					Value volumeArg = invokeExpr.getArg(0);
					//Check if volume is non-negative
					logger.debug("bound: {}", stateBefore.getBound(volumeArg));
					if(stateBefore.getBound(volumeArg).inf().cmp(0) == -1) {
						return false;
					}
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean checkFitsInTrolley() {
		// TODO: CHECK
		for (SootMethod method : this.c.getMethods()){
			for (CallToDelivery callToDelivery : findCallToGetDeliveries(method)) {
				logger.debug("Checking trolley volume for call to delivery");
				NumericalStateWrapper stateBefore = callToDelivery.getStateBefore();
				// Check if state is not bottom
				if (!stateBefore.isBottom()){
					JVirtualInvokeExpr invokeExpr = callToDelivery.getInvokeExpr();
					Value volumeArg = invokeExpr.getArg(0);
					logger.debug("bound: {}", stateBefore.getBound(volumeArg));
					for (StoreInitializer storeInit : this.pointsTo.pointsTo((Local) invokeExpr.getBase())) {
						// Check if trolley volume is not exceeded
						if(stateBefore.getBound(volumeArg).sup().cmp(storeInit.trolley_size) == 1) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean checkFitsInReserve() {
		// TODO: CHECK
		for (SootMethod method : this.c.getMethods()){
			for (CallToDelivery callToDelivery : findCallToGetDeliveries(method)) {
				logger.debug("Checking received amount for call to delivery");
				NumericalStateWrapper stateBefore = callToDelivery.getStateBefore();
				// Check if state is not bottom
				if (!stateBefore.isBottom()){
					NumericalStateWrapper stateAfter = callToDelivery.getStateAfter();
					JVirtualInvokeExpr invokeExpr = callToDelivery.getInvokeExpr();
					for (StoreInitializer storeInit : this.pointsTo.pointsTo((Local) invokeExpr.getBase())) {
						// Check if reserve size is not exceeded (after get_delivery happens)
						logger.debug("bound: {}", stateAfter.getBound(storeInit.getUniqueLabel()));
						if(stateAfter.getBound(storeInit.getUniqueLabel()).sup().cmp(storeInit.reserve_size) == 1) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	// TODO: MAYBE FILL THIS OUT: add convenience methods

	// Get all the occurrences of Store.getDelivery
	private Collection<CallToDelivery> findCallToGetDeliveries(SootMethod method) {
		Collection<CallToDelivery> callToDeliveries = new LinkedList<>();
		for (Unit unit : method.retrieveActiveBody().getUnits()) {
			if (unit instanceof JInvokeStmt) {
				JInvokeStmt invokeStmt = (JInvokeStmt) unit;
				if (invokeStmt.getInvokeExpr() instanceof JVirtualInvokeExpr) {
					JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) invokeStmt.getInvokeExpr();
					//Check if the method is get_delivery of a Store
					if(virtualInvokeExpr.getBase().getType().toString().equals(Constants.StoreClassName)){
						if (virtualInvokeExpr.getMethod().getName().equals(Constants.getDeliveryFunctionName)) {
							callToDeliveries.add(new CallToDelivery(method, numericalAnalysis.get(method), invokeStmt));
						}
					}					
				}
			}
		}
		return callToDeliveries;
	}

}
