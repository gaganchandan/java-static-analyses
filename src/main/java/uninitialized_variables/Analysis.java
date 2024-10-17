package uninitialized_variables;

import java.util.*;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class Analysis {
  private static Body body = null;
  private static ExceptionalUnitGraph graph = null;
  private static SootMethod targetMethod = null;
  private static List<Unit> worklistArray = new ArrayList<Unit>();
  private static Queue<Unit> worklist =
      new LinkedList<Unit>(); // Worklist containing the nodes (units)
  private static HashMap<Unit, Set<Object>> values =
      new HashMap<Unit, Set<Object>>(); // Map of nodes to lattice values at the
  // corresponding program points
  private static Set<Object> init =
      new HashSet<Object>(); // Initial value at the entry of the first node

  public static void main(String[] args) {
    String targetDirectory = args[0];
    String mClass = args[1];
    String tMethod = args[2];
    boolean methodFound = false;

    List<String> procDir = new ArrayList<String>();
    procDir.add(targetDirectory);

    soot.G.reset();
    Options.v().set_process_dir(procDir);
    Options.v().set_src_prec(Options.src_prec_only_class);
    Options.v().set_whole_program(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_output_format(Options.output_format_none);
    Options.v().set_keep_line_number(true);
    Options.v().setPhaseOption("cg.spark", "verbose:false");

    Scene.v().loadNecessaryClasses();

    SootClass entryClass = Scene.v().getSootClassUnsafe(mClass);
    SootMethod entryMethod = entryClass.getMethodByNameUnsafe("main");
    targetMethod = entryClass.getMethodByNameUnsafe(tMethod);

    Options.v().set_main_class(mClass);
    Scene.v().setEntryPoints(Collections.singletonList(entryMethod));

    Iterator mi = entryClass.getMethods().iterator();
    while (mi.hasNext()) {
      SootMethod sm = (SootMethod) mi.next();
      if (sm.getName().equals(tMethod)) {
        methodFound = true;
        break;
      }
    }
    if (!methodFound) {
      System.out.println("Method not found");
      System.exit(1);
    }

    body = targetMethod.retrieveActiveBody();
    graph = new ExceptionalUnitGraph(body);
    worklistArray = getUnits();
    genInit();
    // The values at each node are initialized to the bottom value, that is,
    // the empty set
    for (Unit elem : worklistArray) {
      values.put(elem, new HashSet<Object>());
    }
    worklist = new LinkedList<Unit>(worklistArray);
    doAnalysis();
    for (Unit u : worklistArray) {
      System.out.print("Line: ");
      System.out.println(u);
      System.out.print("Values: ");
      System.out.println(values.get(u));
      System.out.println("\n");
    }
  }

  // Initial values are generated as the least precise value, that is, the set
  // of all locals
  private static void genInit() {
    for (Object l : body.getLocals()) {
      init.add(l);
    }
  }

  private static Set<Object> transferFunction(Unit u, Set<Object> in) {
    // Get all variable definitions in the node.
    List<soot.ValueBox> defs = u.getDefBoxes();
    // If there are no defintions, transfer function is identity.
    if (defs.size() > 0) {
      for (soot.ValueBox def : defs) {
        // Defined locals are removed from the value set at the node.
        // Specifically, the set of defined locals forms the kill set at the
        // node and is removed from the in set to give the out set. There is
        // no gen set.
        if (def.getValue() instanceof soot.jimple.internal.JimpleLocal) {
          in.remove(def.getValue());
        }
      }
    }
    return in;
  }

  // Kildall implementation
  private static void doAnalysis() {
    while (!worklist.isEmpty()) {
      Unit u = worklist.poll();
      Set<Object> in = new HashSet<Object>();
      Set<Object> out = new HashSet<Object>();
      if (graph.getPredsOf(u).isEmpty()) {
        // In set of first node (node with no predecessors) is the initial
        // value
        in = init;
      } else {
        for (Unit pred : graph.getPredsOf(u)) {
          // Generate in set using the join operation, that is, union of all
          // out sets of predecessors
          in.addAll(values.get(pred));
        }
      }
      // New value is given by the transfer function
      out = transferFunction(u, in);
      if (!out.equals(values.get(u))) {
        values.put(u, out);
        // If value at the node has changed, add all successors to the
        // worklist
        for (Unit succ : graph.getSuccsOf(u)) {
          worklist.add(succ);
        }
      }
    }
  }

  private static List<Unit> getUnits() {
    List<Unit> units = new ArrayList<Unit>();
    body = targetMethod.retrieveActiveBody();
    int lineno = 0;
    for (Unit u : body.getUnits()) {
      units.add(u);
      lineno++;
    }
    return units;
  }
}
