/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.parser;


import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;

/**
 * Class to parse pdsc files 
 */
public class PdscParser extends CpXmlParser  {
	

	public PdscParser() {
	}

	public PdscParser(String xsdFile) {
		super(xsdFile);
	}
	
	@Override
	public ICpItem createItem(ICpItem parent, String tag) {
		if(parent != null) {
			return parent.createItem(parent, tag);
		}
		if(rootItem == null) {
			ICpPack pack = new CpPack(parent, tag) ;
			pack.setFileName(xmlFile);
			rootItem = pack;
		}
		return rootItem;
	}
}
