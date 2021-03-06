/*******************************************************************************
 * Copyright (c) 2014 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.CpPlugIn;

/**
 * Utility class to manipulate device vendor strings
 *
 */
public class Vendor {
	protected static Map<String, String> nameToId = null;
	protected static Map<String, String> idToName = null;
	protected static Map<String, String> idToId = null;

	/**
	 * Returns vendor ID
	 * 
	 * @param vendorName
	 * @return vendor id if found, null otherwise
	 */
	public static String getVendorId(String vendorName) {
		if (vendorName == null || vendorName.isEmpty())
			return null;

		// if ID is given in input string, simply return it
		int i = vendorName.indexOf(':');
		if (i >= 0) {
			return vendorName.substring(i + 1);
		}

		if (nameToId == null)
			fillMaps();
		return nameToId.get(vendorName);
	}

	public static String getVendorName(String id) {
		if (id == null || id.isEmpty())
			return null;

		if (idToName == null)
			fillMaps();
		return idToName.get(id);
	}

	/**
	 * Returns official vendor name, for example input <code>"ST:13"</code> will
	 * produce <code>"STMicroelectronics"</code>
	 * 
	 * @param vendor string containing vendor name and id (optionally)
	 * @return official vendor name
	 */
	public static String getOfficialVendorName(String vendor) {
		String id = getVendorId(vendor);
		if (id != null) {
			String name = getVendorName(id);
			if (name == null) { // no such vendor known
				int i = vendor.indexOf(':');
				if (i >= 0) {
					return vendor.substring(0, i); // return its name
				}
			}
			return name;
		}
		return vendor; // simply return vendor as is (mapping does not exist)
	}

	/**
	 * Returns vendor string in canonical format: <code>"official name:id"</code>
	 * @param vendor name or string with id 
	 * @return vendor string in canonical format
	 */
	public static String getCanonicalVendorString(String vendor) {
		String canonical = getOfficialVendorName(vendor);
		String id = getOfficialVenorId(getVendorId(canonical));
		if(id != null) {
			return canonical + ":" + id;
		}	
		return vendor; // simply return string as is (mapping does not exist)
	}

	/**
	 * Converts old vendor ID to new one if needed
	 * @param id vendor ID to convert
	 * @return the official vendor ID if mapping exists, otherwise unchanged id  
	 */
	public static String getOfficialVenorId(String id) {
		if (idToId == null)
			fillMaps();
		if(idToId.containsKey(id))
			return idToId.get(id);
		return id; 
	}
	
	
	/**
	 * Compares two vendor string given in format <code>name:ID</code> First
	 * compares ID's if both present. If they do not match, the names are
	 * compared (may contain wild cards)
	 * 
	 * @param v0
	 *            first vendor string to compare
	 * @param v1
	 *            second vendor string to compare
	 * @return true if strings match each other
	 */
	public static boolean match(final String v0, final String v1) {
		if(v0 == null || v1 == null)
			return false;   
		if(v0.isEmpty() || v1.isEmpty())
			return true;   
		
		String id0 = getOfficialVenorId(getVendorId(v0));
		String id1 = getOfficialVenorId(getVendorId(v1));
		if(id0 != null && id1 != null)
			return id0.equals(id1);
		
		String name0 = getOfficialVendorName(v0);
		String name1 = getOfficialVendorName(v1);
		return WildCards.match(name0,  name1);
	}

	/**
	 * Adds new vendor mapping or replaces existing one (if vendor name changes)
	 * 
	 * @param id
	 *            numeric vendor ID
	 * @param name
	 *            official vendor name
	 */
	public static void addVendor(final String id, final String name) {
		if (idToName == null)
			fillMaps(); // ensure maps are filled
		idToName.put(id, name);
		nameToId.put(name, id);
	}

	/**
	 * Fills internal vendor maps with data provided in the following format:
	 * id[|oldId|oldId]=name[|alias|alias];id=name[|alias|alias]... For example: 82 = ARM;13 =
	 * STMicroelectronics|ST;7=Infineon
	 * 
	 * @param vendors string containing vendor mappings mappings
	 */
	protected static void fillMaps(final String vendors) {
		nameToId = null;
		idToName = null;
		if (vendors == null)
			return;
		nameToId = new HashMap<String, String>();
		idToName = new HashMap<String, String>();
		idToId   = new HashMap<String, String>();
		String[] lines = vendors.split(";");

		for (String line : lines) {
			if (line.isEmpty())
				continue;
			if (!Character.isDigit(line.charAt(0)))
				continue;

			String[] tokens = line.split("=");
			if (tokens.length != 2)
				continue;
			String[] ids = tokens[0].split("\\|");
			if (ids.length < 1)
				continue;

			String id = tokens[0].trim();
			if(id.isEmpty())
				continue;
			for(String oldId : ids) {
				if(!oldId.isEmpty() && !oldId.equals(id))
					idToId.put(oldId, id);
			}
			
			String[] names = tokens[1].split("\\|");
			if (names.length < 1)
				continue;
			String offcialName= names[0].trim();
			idToName.put(id, offcialName);
			for (String name : names) {
				if(!name.isEmpty())
					nameToId.put(name.trim(), id);
			}
		}
	}

	/**
	 * Fills dictionaries with vendor mapping
	 */
	public static void fillMaps() {
		try {
			InputStream inputStream = FileLocator.openStream(CpPlugIn
					.getDefault().getBundle(),
					new Path("resources/vendors.txt"), false);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					inputStream));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line.trim());
				sb.append(";");
			}
			in.close();
			String vendors = sb.toString();
			fillMaps(vendors);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
