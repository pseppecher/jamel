package jamel.models.modelJEE.firms;

import jamel.models.modelJEE.roles.Corporation;
import jamel.models.util.AccountHolder;
import jamel.models.util.Employer;
import jamel.models.util.Supplier;

/**
 * Represents an individual firm.
 */
interface Firm extends AccountHolder, Corporation, Supplier, Employer {

}
