package hu.bme.mit.yakindu.analysis.workhere;

import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.yakindu.sct.model.sgraph.State;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sgraph.Transition;

import hu.bme.mit.model2gml.Model2GML;
import hu.bme.mit.yakindu.analysis.modelmanager.ModelManager;

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
			}
		
		// Transforming the model into a graph representation
		String content = model2gml.transform(root);
		// and saving it
		manager.saveFile("model_output/graph.gml", content);
	}
}
