/*
 * Copyright (C) 2011 OSBI Ltd
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */
package org.saiku.service.datasource;

import java.util.List;
import java.util.Map;

import org.saiku.datasources.datasource.SaikuDatasource;

public interface IDatasourceManager {
	
	public void load();
	
	public SaikuDatasource addDatasource(SaikuDatasource datasource);
	
	public SaikuDatasource setDatasource(SaikuDatasource datasource);
	
	public List<SaikuDatasource> addDatasources(List<SaikuDatasource> datasources);
	
	public boolean removeDatasource(String datasourceName);
	
	public Map<String, SaikuDatasource> getDatasources();

	public SaikuDatasource getDatasource(String datasourceName);

}
