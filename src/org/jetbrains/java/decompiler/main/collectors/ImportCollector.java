/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package org.jetbrains.java.decompiler.main.collectors;

import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.StructField;

import java.util.*;
import java.util.Map.Entry;

public class ImportCollector {
  private static final String JAVA_LANG_PACKAGE = "java.lang";

  private final Map<String, String> mapSimpleNames = new HashMap<String, String>();
  private final Set<String> setNotImportedNames = new HashSet<String>();
  // set of field names in this class and all its predecessors.
  private final Set<String> setFieldNames = new HashSet<String>();
  private final String currentPackageSlash;
  private final String currentPackagePoint;

  public ImportCollector(ClassNode root) {
    String clName = root.classStruct.qualifiedName;
    int index = clName.lastIndexOf('/');
    if (index >= 0) {
      String packageName = clName.substring(0, index);
      currentPackageSlash = packageName + '/';
      currentPackagePoint = packageName.replace('/', '.');
    }
    else {
      currentPackageSlash = "";
      currentPackagePoint = "";
    }

    Map<String, StructClass> classes = DecompilerContext.getStructContext().getClasses();
    StructClass currentClass = root.classStruct;
    while (currentClass != null) {
      // all field names for the current class ..
      for (StructField f : currentClass.getFields()) {
        setFieldNames.add(f.getName());
      }

      // .. and traverse through parent.
      currentClass = currentClass.superClass != null ? classes.get(currentClass.superClass.getString()) : null;
    }
  }

  /**
   * Check whether the package-less name ClassName is shaded by variable in a context of
   * the decompiled class
   * @param classToName - pkg.name.ClassName - class to find shortname for
   * @return ClassName if the name is not shaded by local field, pkg.name.ClassName otherwise
   */
  public String getShortNameInClassContext(String classToName) {
    String shortName = getShortName(classToName);
    if (setFieldNames.contains(shortName)) {
      return classToName;
    }
    else {
      return shortName;
    }
  }

  public String getShortName(String fullName) {
    return getShortName(fullName, true);
  }

  public String getShortName(String fullName, boolean imported) {
    ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(fullName.replace('.', '/')); //todo[r.sh] anonymous classes?

    String result = null;
    if (node != null && node.classStruct.isOwn()) {
      result = node.simpleName;

      while (node.parent != null && node.type == ClassNode.CLASS_MEMBER) {
        //noinspection StringConcatenationInLoop
        result = node.parent.simpleName + '.' + result;
        node = node.parent;
      }

      if (node.type == ClassNode.CLASS_ROOT) {
        fullName = node.classStruct.qualifiedName;
        fullName = fullName.replace('/', '.');
      }
      else {
        return result;
      }
    }
    else {
      fullName = fullName.replace('$', '.');
    }

    String shortName = fullName;
    String packageName = "";

    int lastDot = fullName.lastIndexOf('.');
    if (lastDot >= 0) {
      shortName = fullName.substring(lastDot + 1);
      packageName = fullName.substring(0, lastDot);
    }

    StructContext context = DecompilerContext.getStructContext();

    // check for another class which could 'shadow' this one. Two cases:
    // 1) class with the same short name in the current package
    // 2) class with the same short name in the default package
    boolean existsDefaultClass =
      (context.getClass(currentPackageSlash + shortName) != null && !packageName.equals(currentPackagePoint)) || // current package
      (context.getClass(shortName) != null && !currentPackagePoint.isEmpty());  // default package

    if (existsDefaultClass ||
        (mapSimpleNames.containsKey(shortName) && !packageName.equals(mapSimpleNames.get(shortName)))) {
      //  don't return full name because if the class is a inner class, full name refers to the parent full name, not the child full name
      return result == null ? fullName : (packageName + "." + result);
    }
    else if (!mapSimpleNames.containsKey(shortName)) {
      mapSimpleNames.put(shortName, packageName);
      if (!imported) {
        setNotImportedNames.add(shortName);
      }
    }

    return result == null ? shortName : result;
  }

  public int writeImports(TextBuffer buffer) {
    int importLinesWritten = 0;

    List<String> imports = packImports();

    for (String s : imports) {
      buffer.append("import ");
      buffer.append(s);
      buffer.append(';');
      buffer.appendLineSeparator();

      importLinesWritten++;
    }

    return importLinesWritten;
  }

  private List<String> packImports() {

	List<Entry<String, String>> filtered = new LinkedList<Entry<String, String>>();

	for (Entry<String, String> entry : mapSimpleNames.entrySet()) {
		if (!setNotImportedNames.contains(entry.getKey()) && !entry.getValue().isEmpty()
				&& !JAVA_LANG_PACKAGE.equals(entry.getValue()) && !entry.getValue().equals(currentPackagePoint)) {
			filtered.add(entry);
		}
	}

	List<Entry<String, String>> sorted = new LinkedList<Entry<String, String>>(filtered);

	Collections.sort(sorted, new Comparator<Entry<String, String>>() {
		@Override
		public int compare(Entry<String, String> o1, Entry<String, String> o2) {
			int result = o1.getValue().compareTo(o2.getValue());

			if (result == 0) {
				result = o1.getKey().compareTo(o2.getKey());
			}

			return result;
		}
	});

	List<String> mapped = new LinkedList<String>();

	for (Entry<String, String> entry : sorted) {
		mapped.add(entry.getValue() + "." + entry.getKey());
	}

	return mapped;
  }
}
