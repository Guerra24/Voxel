/*
 * This file is part of Voxel
 * 
 * Copyright (C) 2016-2018 Lux Vacuos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.luxvacuos.voxel.universal.world;

import java.util.Collection;

import net.luxvacuos.lightengine.universal.resources.IDisposable;
import net.luxvacuos.lightengine.universal.util.IUpdatable;
import net.luxvacuos.voxel.universal.world.dimension.IDimension;

public interface IWorld extends IUpdatable, IDisposable {

	// World Stuff
	public String getName();

	// Dimension stuff
	public void addDimension(IDimension dimension);
	
	public void loadDimension(int id);
	
	public IDimension getDimension(int id);

	public void setActiveDimension(int id);

	public IDimension getActiveDimension();

	public Collection<IDimension> getDimensions();

}
