package jamel.basic.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An abstract dispatcher.
 */
public abstract class Dispatcher {
	
	/** The arguments of the request to dispatch. */
	private Object[] args;
	
	/**
	 * Returns the arguments. 
	 * @return an array of objects.
	 */
	protected Object[] getArgs() {
		return args;
	}
	
	/**
	 * Called when no method reflects the request in the dispatcher. Generates a <code>RuntimeException</code>.
	 * @param request the request.
	 * @return nothing.
	 */
	protected Object redirect(String request) {
		throw new IllegalArgumentException("No such method: "+request);
	}
		
	/**
	 * Forwards a request to the resource.
	 * @param request the request to forward.
	 * @param args an array of objects that contains the parameters of the request.
	 * @return an object.
	 */
	public final synchronized Object forward(String request, Object... args) {
		this.args=args;
		Object result;
		try {
			final Method m = this.getClass().getMethod(request);
			m.setAccessible(true);
			result = m.invoke(this);
			m.setAccessible(false);
		} catch (NoSuchMethodException e) {
			result = redirect(request);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException("Error while handling the request: "+request);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Error while handling the request: "+request);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException("Error while handling the request: "+request);
		}
		this.args=null;
		return result;
	}
	
}

// ***
