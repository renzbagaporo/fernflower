/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package org.jetbrains.java.decompiler.main.extern;

import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class FernflowerPreferences {
  public static String REMOVE_BRIDGE = "rbr";
  public static String REMOVE_SYNTHETIC = "rsy";
  public static String DECOMPILE_INNER = "din";
  public static String DECOMPILE_CLASS_1_4 = "dc4";
  public static String DECOMPILE_ASSERTIONS = "das";
  public static String HIDE_EMPTY_SUPER = "hes";
  public static String HIDE_DEFAULT_CONSTRUCTOR = "hdc";
  public static String DECOMPILE_GENERIC_SIGNATURES = "dgs";
  public static String NO_EXCEPTIONS_RETURN = "ner";
  public static String DECOMPILE_ENUM = "den";
  public static String REMOVE_GET_CLASS_NEW = "rgn";
  public static String LITERALS_AS_IS = "lit";
  public static String BOOLEAN_TRUE_ONE = "bto";
  public static String ASCII_STRING_CHARACTERS = "asc";
  public static String SYNTHETIC_NOT_SET = "nns";
  public static String UNDEFINED_PARAM_TYPE_OBJECT = "uto";
  public static String USE_DEBUG_VAR_NAMES = "udv";
  public static String USE_METHOD_PARAMETERS = "ump";
  public static String REMOVE_EMPTY_RANGES = "rer";
  public static String FINALLY_DEINLINE = "fdi";
  public static String IDEA_NOT_NULL_ANNOTATION = "inn";
  public static String LAMBDA_TO_ANONYMOUS_CLASS = "lac";
  public static String BYTECODE_SOURCE_MAPPING = "bsm";

  public static String LOG_LEVEL = "log";
  public static String MAX_PROCESSING_METHOD = "mpm";
  public static String RENAME_ENTITIES = "ren";
  public static String USER_RENAMER_CLASS = "urc";
  public static String NEW_LINE_SEPARATOR = "nls";
  public static String INDENT_STRING = "ind";
  public static String BANNER = "ban";

  public static String DUMP_ORIGINAL_LINES = "__dump_original_lines__";
  public static String UNIT_TEST_MODE = "__unit_test_mode__";

  public static String LINE_SEPARATOR_WIN = "\r\n";
  public static String LINE_SEPARATOR_UNX = "\n";

  public static Map<String, Object> DEFAULTS = getDefaults();

  static Map<String, Object> getDefaults() {
    Map<String, Object> defaults = new HashMap<>();

    defaults.put(REMOVE_BRIDGE, "1");
    defaults.put(REMOVE_SYNTHETIC, "0");
    defaults.put(DECOMPILE_INNER, "1");
    defaults.put(DECOMPILE_CLASS_1_4, "1");
    defaults.put(DECOMPILE_ASSERTIONS, "1");
    defaults.put(HIDE_EMPTY_SUPER, "1");
    defaults.put(HIDE_DEFAULT_CONSTRUCTOR, "1");
    defaults.put(DECOMPILE_GENERIC_SIGNATURES, "0");
    defaults.put(NO_EXCEPTIONS_RETURN, "1");
    defaults.put(DECOMPILE_ENUM, "1");
    defaults.put(REMOVE_GET_CLASS_NEW, "1");
    defaults.put(LITERALS_AS_IS, "0");
    defaults.put(BOOLEAN_TRUE_ONE, "1");
    defaults.put(ASCII_STRING_CHARACTERS, "0");
    defaults.put(SYNTHETIC_NOT_SET, "0");
    defaults.put(UNDEFINED_PARAM_TYPE_OBJECT, "1");
    defaults.put(USE_DEBUG_VAR_NAMES, "1");
    defaults.put(USE_METHOD_PARAMETERS, "1");
    defaults.put(REMOVE_EMPTY_RANGES, "1");
    defaults.put(FINALLY_DEINLINE, "1");
    defaults.put(IDEA_NOT_NULL_ANNOTATION, "1");
    defaults.put(LAMBDA_TO_ANONYMOUS_CLASS, "0");
    defaults.put(BYTECODE_SOURCE_MAPPING, "0");

    defaults.put(LOG_LEVEL, IFernflowerLogger.Severity.INFO.name());
    defaults.put(MAX_PROCESSING_METHOD, "0");
    defaults.put(RENAME_ENTITIES, "0");
    defaults.put(NEW_LINE_SEPARATOR, (InterpreterUtil.IS_WINDOWS ? "0" : "1"));
    defaults.put(INDENT_STRING, "   ");
    defaults.put(BANNER, "");
    defaults.put(UNIT_TEST_MODE, "0");
    defaults.put(DUMP_ORIGINAL_LINES, "0");

    return Collections.unmodifiableMap(defaults);
  }
}