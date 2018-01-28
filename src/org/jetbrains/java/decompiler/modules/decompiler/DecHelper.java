// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;

import java.util.*;


public class DecHelper {

  public static boolean checkStatementExceptions(List<Statement> lst) {

    Set<Statement> all = new HashSet<Statement>(lst);

    Set<Statement> handlers = new HashSet<Statement>();
    Set<Statement> intersection = null;

    for (Statement stat : lst) {
      Set<Statement> setNew = stat.getNeighboursSet(StatEdge.TYPE_EXCEPTION, Statement.DIRECTION_FORWARD);

      if (intersection == null) {
        intersection = setNew;
      }
      else {
        HashSet<Statement> interclone = new HashSet<Statement>(intersection);
        interclone.removeAll(setNew);

        intersection.retainAll(setNew);

        setNew.removeAll(intersection);

        handlers.addAll(interclone);
        handlers.addAll(setNew);
      }
    }

    for (Statement stat : handlers) {
      if (!all.contains(stat) || !all.containsAll(stat.getNeighbours(StatEdge.TYPE_EXCEPTION, Statement.DIRECTION_BACKWARD))) {
        return false;
      }
    }

    // check for other handlers (excluding head)
    for (int i = 1; i < lst.size(); i++) {
      Statement stat = lst.get(i);
      if (!stat.getPredecessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty() && !handlers.contains(stat)) {
        return false;
      }
    }

    return true;
  }

  public static boolean isChoiceStatement(Statement head, List<Statement> lst) {

    Statement post = null;

    Set<Statement> setDest = head.getNeighboursSet(StatEdge.TYPE_REGULAR, Statement.DIRECTION_FORWARD);

    if (setDest.contains(head)) {
      return false;
    }

    while (true) {

      lst.clear();

      boolean repeat = false;

      setDest.remove(post);

      for (Statement stat : setDest) {
        if (stat.getLastBasicType() != Statement.LASTBASICTYPE_GENERAL) {
          if (post == null) {
            post = stat;
            repeat = true;
            break;
          }
          else {
            return false;
          }
        }

        // preds
        Set<Statement> setPred = stat.getNeighboursSet(StatEdge.TYPE_REGULAR, Statement.DIRECTION_BACKWARD);
        setPred.remove(head);
        if (setPred.contains(stat)) {
          return false;
        }

        if (!setDest.containsAll(setPred) || setPred.size() > 1) {
          if (post == null) {
            post = stat;
            repeat = true;
            break;
          }
          else {
            return false;
          }
        }
        else if (setPred.size() == 1) {
          Statement pred = setPred.iterator().next();
          while (lst.contains(pred)) {
            Set<Statement> setPredTemp = pred.getNeighboursSet(StatEdge.TYPE_REGULAR, Statement.DIRECTION_BACKWARD);
            setPredTemp.remove(head);

            if (!setPredTemp.isEmpty()) { // at most 1 predecessor
              pred = setPredTemp.iterator().next();
              if (pred == stat) {
                return false;  // loop found
              }
            }
            else {
              break;
            }
          }
        }

        // succs
        List<StatEdge> lstEdges = stat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL);
        if (lstEdges.size() > 1) {
          Set<Statement> setSucc = stat.getNeighboursSet(Statement.STATEDGE_DIRECT_ALL, Statement.DIRECTION_FORWARD);
          setSucc.retainAll(setDest);

          if (setSucc.size() > 0) {
            return false;
          }
          else {
            if (post == null) {
              post = stat;
              repeat = true;
              break;
            }
            else {
              return false;
            }
          }
        }
        else if (lstEdges.size() == 1) {

          StatEdge edge = lstEdges.get(0);
          if (edge.getType() == StatEdge.TYPE_REGULAR) {
            Statement statd = edge.getDestination();
            if (head == statd) {
              return false;
            }
            if (!setDest.contains(statd) && post != statd) {
              if (post != null) {
                return false;
              }
              else {
                Set<Statement> set = statd.getNeighboursSet(StatEdge.TYPE_REGULAR, Statement.DIRECTION_BACKWARD);
                if (set.size() > 1) {
                  post = statd;
                  repeat = true;
                  break;
                }
                else {
                  return false;
                }
              }
            }
          }
        }

        lst.add(stat);
      }

      if (!repeat) {
        break;
      }
    }

    lst.add(head);
    lst.remove(post);

    lst.add(0, post);

    return true;
  }

  public static Set<Statement> getUniquePredExceptions(Statement head) {
    Set<Statement> setHandlers = new HashSet<Statement>(head.getNeighbours(StatEdge.TYPE_EXCEPTION, Statement.DIRECTION_FORWARD));
    
    List<Statement> toRemove = new LinkedList<Statement>();
    
    for(Statement statement : setHandlers) {
    	if(statement.getPredecessorEdges(StatEdge.TYPE_EXCEPTION).size() > 1) {
    		toRemove.add(statement);
    	}
    }
    
    setHandlers.removeAll(toRemove);
    
    return setHandlers;
  }

  public static List<Exprent> copyExprentList(List<Exprent> lst) {
    List<Exprent> ret = new ArrayList<Exprent>();
    for (Exprent expr : lst) {
      ret.add(expr.copy());
    }
    return ret;
  }
}