package com.pmease.commons.xmt;

/**
 * This listener class is used as a callback when migration is finished.
 * @author robin
 *
 */
public interface MigrationListener {
	/**
	 * Called after migration. 
	 * @param bean the bean object after migration
	 */
	void migrated(Object bean);
}
