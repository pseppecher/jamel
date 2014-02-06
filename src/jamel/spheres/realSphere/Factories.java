/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.spheres.realSphere;

import jamel.agents.firms.util.Mediator;
import jamel.agents.firms.util.ProductionType;

/**
 *
 */
public class Factories {
	
	/**
	 * Returns a new factory.
	 * @param sector  the sector.
	 * @param mediator  the mediator.
	 * @return a new factory.
	 */
	static public Factory getNewFactory(ProductionType sector,Mediator mediator) {
		switch (sector) {
		case intermediateProduction:
			return new IntermediateFactory(mediator);
		case finalProduction:{
			return new FinalFactory(mediator);
		}
		case integratedProduction:
			return new IntegratedFactory(mediator);
		default :
			throw new RuntimeException("Error while creating new factory: unknown sector."); 
		}			
	}

}
