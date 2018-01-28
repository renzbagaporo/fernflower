// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.java.decompiler.modules.decompiler.decompose.GenericDominatorEngine;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.IGraph;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.IGraphNode;
import org.jetbrains.java.decompiler.util.VBStyleCollection;

import java.util.*;
import java.util.function.Function;

public class VarVersionsGraph {
  public final VBStyleCollection<VarVersionNode, VarVersionPair> nodes = new VBStyleCollection<VarVersionNode, VarVersionPair>();

  private GenericDominatorEngine engine;

  public VarVersionNode createNode(VarVersionPair ver) {
    VarVersionNode node;
    nodes.addWithKey(node = new VarVersionNode(ver.var, ver.version), ver);
    return node;
  }

  public void addNodes(Collection<VarVersionNode> colnodes, Collection<VarVersionPair> colpaars) {
    nodes.addAllWithKey(colnodes, colpaars);
  }

  public boolean isDominatorSet(VarVersionNode node, Set<VarVersionNode> domnodes) {
    if (domnodes.size() == 1) {
      return engine.isDominator(node, domnodes.iterator().next());
    }
    else {
      Set<VarVersionNode> marked = new HashSet<VarVersionNode>();

      if (domnodes.contains(node)) {
        return true;
      }

      List<VarVersionNode> lstNodes = new LinkedList<VarVersionNode>();
      lstNodes.add(node);

      while (!lstNodes.isEmpty()) {
        VarVersionNode nd = lstNodes.remove(0);
        if (marked.contains(nd)) {
          continue;
        }
        else {
          marked.add(nd);
        }

        if (nd.preds.isEmpty()) {
          return false;
        }

        for (VarVersionEdge edge : nd.preds) {
          VarVersionNode pred = edge.source;
          if (!marked.contains(pred) && !domnodes.contains(pred)) {
            lstNodes.add(pred);
          }
        }
      }
    }

    return true;
  }

  public void initDominators() {
    final Set<VarVersionNode> roots = new HashSet<VarVersionNode>();

    for (VarVersionNode node : nodes) {
      if (node.preds.isEmpty()) {
        roots.add(node);
      }
    }

    engine = new GenericDominatorEngine(new IGraph() {
      public List<? extends IGraphNode> getReversePostOrderList() {
        return getReversedPostOrder(roots);
      }

      public Set<? extends IGraphNode> getRoots() {
        return new HashSet<IGraphNode>(roots);
      }
    });

    engine.initialize();
  }

  private static List<VarVersionNode> getReversedPostOrder(Collection<VarVersionNode> roots) {
    List<VarVersionNode> lst = new LinkedList<VarVersionNode>();
    Set<VarVersionNode> setVisited = new HashSet<VarVersionNode>();

    for (VarVersionNode root : roots) {
      List<VarVersionNode> lstTemp = new LinkedList<VarVersionNode>();
      addToReversePostOrderListIterative(root, lstTemp, setVisited);
      lst.addAll(lstTemp);
    }

    return lst;
  }

  private static void addToReversePostOrderListIterative(VarVersionNode root, List<VarVersionNode> lst, Set<VarVersionNode> setVisited) {
    Map<VarVersionNode, List<VarVersionEdge>> mapNodeSuccs = new HashMap<VarVersionNode, List<VarVersionEdge>>();
    LinkedList<VarVersionNode> stackNode = new LinkedList<VarVersionNode>();
    LinkedList<Integer> stackIndex = new LinkedList<Integer>();

    stackNode.add(root);
    stackIndex.add(0);

    while (!stackNode.isEmpty()) {
      VarVersionNode node = stackNode.getLast();
      int index = stackIndex.removeLast();

      setVisited.add(node);
      
      List<VarVersionEdge> lstSuccs  = mapNodeSuccs.get(node);
		
	  if(lstSuccs == null) {
		lstSuccs = new ArrayList<VarVersionEdge>(node.succs);
		mapNodeSuccs.put(node, lstSuccs);
	  }
	        
      for (; index < lstSuccs.size(); index++) {
        VarVersionNode succ = lstSuccs.get(index).dest;

        if (!setVisited.contains(succ)) {
          stackIndex.add(index + 1);
          stackNode.add(succ);
          stackIndex.add(0);
          break;
        }
      }

      if (index == lstSuccs.size()) {
        lst.add(0, node);
        stackNode.removeLast();
      }
    }
  }
}