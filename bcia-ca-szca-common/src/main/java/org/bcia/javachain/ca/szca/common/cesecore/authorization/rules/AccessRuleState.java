/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.bcia.javachain.ca.szca.common.cesecore.authorization.rules;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id: AccessRuleState.java 17625 2013-09-20 07:12:06Z netmackan $
 * 
 */
public enum AccessRuleState {
    RULE_NOTUSED("UNUSED", 0), RULE_ACCEPT("ACCEPT", 1), RULE_DECLINE("DECLINE", 2);
  
    private AccessRuleState(String name, int databaseValue) {
        this.name = name;
        this.databaseValue = databaseValue;
    }

    public String getName() {
        return name;
    }

    public int getDatabaseValue() {
        return databaseValue;
    } 
    
    public static AccessRuleState matchDatabaseValue(Integer value) {
        return databaseValueToRuleMap.get(value);
    }
    
    public static AccessRuleState matchName(String name) {
        return nameToRuleMap.get(name);
    }
    
    private String name;
    private int databaseValue;
    private static Map<Integer, AccessRuleState> databaseValueToRuleMap = new HashMap<Integer, AccessRuleState>();
    private static Map<String, AccessRuleState> nameToRuleMap = new HashMap<String, AccessRuleState>();
    
    static {
        for(AccessRuleState state : AccessRuleState.values()) {
            databaseValueToRuleMap.put(state.getDatabaseValue(), state);
            nameToRuleMap.put(state.getName(), state);
        }
    }
    

    
    
}
