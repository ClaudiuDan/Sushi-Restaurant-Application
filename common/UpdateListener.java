package common;

/**
 * An update listener using the observer pattern used to stay informed of model updates.
 * This is primarily used by the user interface to be notified when the backend model has been updated and to update the UI.
 * The listener will be called of details of the model (where applicable) and properties (where applicable) that have been updated.
 * Can be used without a model to indicate a general update and to perform a general refresh.
 *
 */
public interface UpdateListener {

	/**
	 * Receive a notification of a model update with the given UpdateEvent containing the update information.
	 * @param updateEvent information on the update
	 */
	public void updated(UpdateEvent updateEvent);

}