// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.attr;

import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
  u2 local_variable_table_length;
  local_variable {
    u2 start_pc;
    u2 length;
    u2 name_index;
    u2 descriptor_index;
    u2 index;
  }
*/
public class StructLocalVariableTableAttribute extends StructGeneralAttribute {
  private List<LocalVariable> localVariables = Collections.emptyList();

  @Override
  public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
    int len = data.readUnsignedShort();
    if (len > 0) {
      localVariables = new ArrayList<LocalVariable>(len);

      for (int i = 0; i < len; i++) {
        int start_pc = data.readUnsignedShort();
        int length = data.readUnsignedShort();
        int nameIndex = data.readUnsignedShort();
        int descriptorIndex = data.readUnsignedShort();
        int varIndex = data.readUnsignedShort();
        localVariables.add(new LocalVariable(start_pc,
                                             length,
                                             pool.getPrimitiveConstant(nameIndex).getString(),
                                             pool.getPrimitiveConstant(descriptorIndex).getString(),
                                             varIndex));
      }
    }
    else {
      localVariables = Collections.emptyList();
    }
  }

  public void add(StructLocalVariableTableAttribute attr) {
    localVariables.addAll(attr.localVariables);
  }

  public String getName(int index, int visibleOffset) {
	List<LocalVariable> matches = matchingVars(index, visibleOffset);
		
	if(matches.size() > 0) {
		return matches.get(0).name;
	}
	else {
		return null;
	}
  }

  public String getDescriptor(int index, int visibleOffset) {
	List<LocalVariable> matches = matchingVars(index, visibleOffset);
	
	if(matches.size() > 0) {
		return matches.get(0).descriptor;
	}
	else {
		return null;
	}
  }
  
  private List<LocalVariable> matchingVars(int index, int visibleOffset)
  {
	  List<LocalVariable> matches = new LinkedList<LocalVariable>();
	  
	  for(LocalVariable v : localVariables) {
		  if(v.index == index && (visibleOffset >= v.start_pc && visibleOffset < v.start_pc + v.length)) {
			  matches.add(v);
		  }
	  }
	  
	  return matches;
  }

  public boolean containsName(final String name) {
	
	for(LocalVariable v : localVariables)
	{
		if(v.name.equals(name))
		{
			return true;
		}
	}
	
	return false;
  }

  public Map<Integer, String> getMapParamNames() {
	
	List<LocalVariable> filtered = new LinkedList<LocalVariable>();
	
	for (LocalVariable localVariable : localVariables) {
		if(localVariable.start_pc == 0)
		{
			filtered.add(localVariable);
		}
	}
	
	Map<Integer, String> mapped = new HashMap<Integer, String>();
	
	for (LocalVariable localVariable : filtered) {
		mapped.put(localVariable.index, localVariable.name);
	}
	
	return mapped;
  }

  private static class LocalVariable {
    final int start_pc;
    final int length;
    final String name;
    final String descriptor;
    final int index;

    private LocalVariable(int start_pc, int length, String name, String descriptor, int index) {
      this.start_pc = start_pc;
      this.length = length;
      this.name = name;
      this.descriptor = descriptor;
      this.index = index;
    }
  }
}
