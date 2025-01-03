package ch.ethz.rse.numerical;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Interval;
import apron.Manager;
import apron.NotImplementedException;
import apron.Tcons1;
import apron.Texpr1Intern;
import soot.Local;
import soot.SootHelper;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;

/**
 * Convenience wrapper for numerical abstract elements in Apron.
 * 
 */
public class NumericalStateWrapper {

	private static final Logger logger = LoggerFactory.getLogger(NumericalStateWrapper.class);

	// STATIC

	public static NumericalStateWrapper bottom(Manager man, Environment env) {
		try {
			Abstract1 bot = new Abstract1(man, env, true);
			return new NumericalStateWrapper(man, bot);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	public static NumericalStateWrapper top(Manager man, Environment env) {
		try {
			Abstract1 top = new Abstract1(man, env);
			return new NumericalStateWrapper(man, top);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	// FIELDS

	/**
	 * Wrapped abstract element
	 */
	private Abstract1 elem;

	/**
	 * Manager for numerical abstract domain
	 */
	private final Manager man;

	// CONSTRUCTOR

	/**
	 * 
	 * @param man  Apron abstract domain manager
	 * @param elem Abstract Apron element
	 */
	public NumericalStateWrapper(Manager man, Abstract1 elem) {
		this.man = man;
		this.elem = elem;
	}

	// FUNCTIONS

	public Abstract1 get() {
		return elem;
	}

	public void set(Abstract1 e) {
		elem = e;
	}

	public NumericalStateWrapper copy() {
		Abstract1 copy;
		try {
			copy = new Abstract1(man, this.elem);
			return new NumericalStateWrapper(this.man, copy);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Copies this state into `other`
	 * 
	 * @param other
	 */
	public void copyInto(NumericalStateWrapper other) {
		NumericalStateWrapper copy = this.copy();
		other.elem = copy.elem;
	}

	// TODO: MAYBE FILL THIS OUT: add convenience methods

	/**
	 * returns bound of variable v (Value)
	 */
	public Interval getBound(Value v){
		try{
			if (v instanceof IntConstant){
				int value = ((IntConstant) v).value;
				return new Interval(value, value);
			} else if (v instanceof Local) {
				return this.elem.getBound(this.man, getVarName(v));
			} else {
				NumericalAnalysis.unhandled("getBound", v, true);
				return null;
			}
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * returns bound of variable v (String)
	 */
	public Interval getBound(String v){
		try{
			return this.elem.getBound(this.man, v);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * returns variable name of Value v
	 * @param v Value
	 * @return variable name of Value v (String)
	 */
	public String getVarName(Value v) {
		if (v instanceof Local) {
			return ((Local) v).getName();
		} else {
			NumericalAnalysis.unhandled("getVarName", v, true);
			return null;
		}
	}

	/**
	 * Apply widening to this.
	 * @param oldState state of the previous iteration
	 */
	public void widening(NumericalStateWrapper oldState){
		try{
			Abstract1 joined = this.elem.joinCopy(this.man, oldState.get());
			Abstract1 widened = oldState.get().widening(man, joined);
			this.set(widened);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * merging this with w2 and put the result in w3
	 * @param w2 merges this with w2
	 * @param w3 and put the result in w3
	 */
	public void merge(NumericalStateWrapper w2, NumericalStateWrapper w3){
		try{
			Abstract1 mergedState = this.elem.joinCopy(this.man, w2.get());
			w3.set(mergedState);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * assign expression to a variable
	 * @param var variable name
	 * @param expr expression
	 */
	public void assign(String var, Texpr1Intern expr){
		try{
			this.elem.assign(this.man, var, expr, null);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * meet constraint
	 * @param constraint
	 */
	public void meet(Tcons1 constraint){
		try{
			this.elem.meet(this.man, constraint);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * isBottom true when the elem is empty
	 * i.e. it is not possible to reach here
	 */
	public boolean isBottom(){
		try{
			return this.elem.isBottom(this.man);
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}

	// EQUALS, HASHCODE, TOSTRING

	@Override
	public boolean equals(Object o) {
		// needed by NumericalAnalysis
		if (!(o instanceof NumericalStateWrapper)) {
			return false;
		}
		NumericalStateWrapper w = (NumericalStateWrapper) o;

		Abstract1 t = w.get();
		try {
			// sanity check
			if (elem.isEqual(man, t) && !elem.isIncluded(man, t)) {
				throw new RuntimeException("VIOLATION");
			}

			return elem.isEqual(man, t);
		} catch (ApronException e) {
			throw new RuntimeException("isEqual failed");
		}
	}

	@Override
	public int hashCode() {
		// implementation non-trivial but not needed
		throw new RuntimeException(new NotImplementedException());
	}

	@Override
	public String toString() {
		try {
			if (elem == null) {
				return "null";
			} else if (elem.isTop(man)) {
				return "<Top>";
			} else {
				return elem.toString();
			}
		} catch (ApronException e) {
			throw new RuntimeException(e);
		}
	}
}
