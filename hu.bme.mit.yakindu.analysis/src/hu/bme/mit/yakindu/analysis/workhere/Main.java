package hu.bme.mit.yakindu.analysis.workhere;

import java.util.ArrayList;
import java.util.List;
import java.lang.*;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.yakindu.sct.model.sgraph.State;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sgraph.Transition;
import org.yakindu.sct.model.stext.stext.EventDefinition;
import org.yakindu.sct.model.stext.stext.VariableDefinition;

import hu.bme.mit.model2gml.Model2GML;
import hu.bme.mit.yakindu.analysis.modelmanager.ModelManager;
import hu.bme.mit.yakindu.analysis.example.IExampleStatemachine;

public class Main {
	@Test
	public void test() {
		main(new String[0]);
	}
	
	public static void main(String[] args) {
		ModelManager manager = new ModelManager();
		Model2GML model2gml = new Model2GML();
		
		// Loading model
		EObject root = manager.loadModel("model_input/example.sct");
		
		// Reading model
		Statechart s = (Statechart) root;
		TreeIterator<EObject> iterator = s.eAllContents();
		
		int noNameCounter = 0;
		List<String> variables = new ArrayList<String>();
		List<String> events = new ArrayList<String>();
		while (iterator.hasNext()) {
			EObject content = iterator.next();
			if(content instanceof State) {
				State state = (State) content;
			
				List<Transition> trs = (state).getOutgoingTransitions();
				if (state.getName().equals(""))
					state.setName(noNameCounter + " state");
				System.out.println(state.getName());
				boolean flag = false;
				for(int i = 0; i < trs.size();i++) {
					String lhs = trs.get(i).getSource().getName();
					String rhs = trs.get(i).getTarget().getName();
					if (rhs.equals("")) {
						trs.get(i).getTarget().setName(noNameCounter + " state");
						noNameCounter++;
					}
					if (!rhs.equals(lhs))
						flag = true;
					System.out.println(String.format("%s -> %s", lhs,rhs));
				}
				if(!flag) {
					System.out.println("This state is a trap");
				}
				else {
					System.out.println("This state is NOT a trap");
	
				}
	
			}
			
			if(content instanceof VariableDefinition) {
				VariableDefinition var = (VariableDefinition) content;
				
				System.out.println(var.getName());
				variables.add(var.getName());
			}
			if(content instanceof EventDefinition) {
				EventDefinition ev = (EventDefinition) content;
				events.add(ev.getName());
				System.out.println(ev.getName());
			}
		}
		
		String codeBegin = "package hu.bme.mit.yakindu.analysis.workhere;\nimport java.io.IOException;\nimport java.util.Scanner;\nimport hu.bme.mit.yakindu.analysis.RuntimeService;\nimport hu.bme.mit.yakindu.analysis.TimerService;\nimport hu.bme.mit.yakindu.analysis.example.ExampleStatemachine;\nimport hu.bme.mit.yakindu.analysis.example.IExampleStatemachine;\npublic class RunStatechart {\npublic static void main(String[] args) throws IOException {\nExampleStatemachine s = new ExampleStatemachine();\ns.setTimer(new TimerService());\nRuntimeService.getInstance().registerStatemachine(s, 200);\ns.init();\ns.enter();\ns.runCycle();\nScanner sc = new Scanner(System.in);\nwhile(true) {\nswitch(sc.nextLine()) {\n";

		List<String> cases = new ArrayList<String>();
		for (String i: events) {
			String eventUpper = i.substring(0, 1).toUpperCase() + i.substring(1);
			cases.add(String.format("case \"%s\": ",i));
			cases.add(String.format("s.raise%s();",eventUpper));
			cases.add("s.runCycle()\n; print(s);");
			cases.add("break;");
			
		}
		
		String codeMidEnd = "case \"exit\":\nprint(s);\nSystem.exit(0);\nbreak;\ndefault:\nprint(s);\nbreak;\n}\n}\n}\n";
		List<String> lines = new ArrayList<String>();
		lines.add("public static void print(IExampleStatemachine s){");
		for (String i: variables) {
			String output = i.substring(0, 1).toUpperCase() + i.substring(1);
			String line = String.format("\tSystem.out.println(\"%c = \" + s.getSCInterface().get%s());", Character.toUpperCase(i.charAt(0)),output);
			lines.add(line);
			
		}
		lines.add("}");
		
		String codeEnd = "}";
		System.out.println(codeBegin);
		cases.forEach(System.out::println);
		System.out.println(codeMidEnd);
		lines.forEach(System.out::println);
		System.out.println(codeEnd);
		// Transforming the model into a graph representation
		String content = model2gml.transform(root);
		// and saving it
		manager.saveFile("model_output/graph.gml", content);
	}
}
