import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import algorithms.YenTopKShortestPathsAlg;
import model.*;
import model.abstracts.BaseVertex;


public class mor {

	public static void main(String[] args) {
		System.out.println("Metaheurísticas y Optimización sobre Redes - 2015");
		
		//Cargo grafo desde archivo
		Graph G = new Graph("data/test_mor");
		G.export_to_file("data/salidas/G_creado.txt");
				
		//Cargo nodos terminales (después deberíamos cargarlos desde un archivo)(por ahora harcodeado)
		List<Integer> T = new ArrayList<Integer>();
		T.add(1);
		T.add(5);
		
		//al momento de cargar los T debo generar esto (por ahora harcodeado)
		Map<Pair<Integer, Integer>, Integer> m_ij = new HashMap<Pair<Integer,Integer>, Integer>();
		m_ij.put(new Pair<Integer, Integer>(1,5),3);
		m_ij.put(new Pair<Integer, Integer>(5,1),3);	
		
		//Creo Pij lista de caminos nodos-disjuntos
		List<Path> Pij = new ArrayList<Path>();
		
		//Creo grafo Gsol cuyos nodos son los terminales y no tiene aristas		
		Graph Gsol = new Graph();
		Gsol.set_vertex_to_graph(T);	
		Gsol.export_to_file("data/salidas/gsol.txt");
		
		//seleccionar randomicamente i,j in TxT /mij>0
		Pair<Integer,Integer> random_ij = get_random_ij(m_ij,T,1);
		
		Map<Pair<Integer, Integer>, Double> costo_techo = new HashMap<Pair<Integer,Integer>, Double>();
		Graph H = G.copy_of_graph();
		Gsol.export_to_file("data/salidas/H_G_copy.txt");
		int iter = 0;
		while ((random_ij!=null) && (iter<30)){ //mientras exista un (i,j) in TxT /mij>0
			for (Path p : Pij){
				H = H.grafo_menos_camino(p);
			}

			costo_techo = new HashMap<Pair<Integer,Integer>, Double>(H.get_vertex_pair_weight_index());
			
			double c_techo = 0;
			Map<Pair<Integer, Integer>, Double> edges_cost = G.get_vertex_pair_weight_index();
			if (edges_cost.get(random_ij)!=null){
				c_techo = edges_cost.get(random_ij); 
			}
			
			costo_techo.put(random_ij, c_techo);
			
			//Yen
			YenTopKShortestPathsAlg yen = new YenTopKShortestPathsAlg(H);
			List<Path> yens_path = yen.get_shortest_paths(H.get_vertex(random_ij.first()),H.get_vertex(random_ij.second()), 5);
			
			if ((yens_path!=null)&&(yens_path.size()>0)){
				Path shortest_path = yens_path.get(0);
				if (shortest_path.get_weight()==0){
					Vertex i = (Vertex) shortest_path.get_vertices().get(0);
					Vertex j = (Vertex) shortest_path.get_vertices().get(shortest_path.get_vertices().size()-1);
					int old_mij = m_ij.get(new Pair<Integer,Integer>(i.get_id(),j.get_id()));
					m_ij.put(new Pair<Integer,Integer>(i.get_id(),j.get_id()),old_mij-1);
					Pij.add(shortest_path);					
				}else{
					Random randomizer = new Random();
					Path p_random = yens_path.get(randomizer.nextInt(yens_path.size()));
					Vertex i = (Vertex) p_random.get_vertices().get(0);
					Vertex j = (Vertex) p_random.get_vertices().get(p_random.get_vertices().size()-1);
					int old_mij = m_ij.get(new Pair<Integer,Integer>(i.get_id(),j.get_id()));
					m_ij.put(new Pair<Integer,Integer>(i.get_id(),j.get_id()),old_mij-1);
					Pij.add(p_random);		
					Gsol.grafo_mas_camino(shortest_path, G);
				}
			}	
			
			Gsol.export_to_file("data/salidas/H_iter_"+iter+".txt");
			iter++;
			random_ij = get_random_ij(m_ij,T,1);
		}
		
		H.export_to_file("data/salidas/G_solucion.txt");
		
		
		
//		Graph G_copy = G.copy_of_graph();
//		G_copy.export_to_file("data/salidas/G_copy.txt");
//		
/*		List<BaseVertex> vertex_list = new ArrayList<BaseVertex>();
		BaseVertex v =  new Vertex();
		v.set_id(0);
		v.set_weight(1);
		vertex_list.add(v);
		v =  new Vertex();
		v.set_id(1);
		v.set_weight(1);
		vertex_list.add(v);
		
		Path p = new Path(vertex_list,3);
		
		Gsol = Gsol.grafo_mas_camino(p, G);
		Gsol.export_to_file("data/salidas/G_mas_camino.txt");*/
//	
//		Graph G_menos_camino = G_copy.grafo_menos_camino(p);
//		G_menos_camino.export_to_file("data/salidas/G_menos_camino.txt");
		
		System.out.println("Fin.");
		
	}
	
	
	private static Pair<Integer,Integer> get_random_ij (Map<Pair<Integer, Integer>, Integer> m_ij, List<Integer> T, int tipo_random){
		if (tipo_random==0){
			//selecciono par i,j in T que cumpla mij>0 totalmente random
			Random randomizer = new Random();
			int i = T.get(randomizer.nextInt(T.size()));
			int j = -1;
			while (j!=i){
				j = T.get(randomizer.nextInt(T.size()));
			}
			
			while (m_ij.get(new Pair<Integer, Integer>(i,j))<=0){
				i = T.get(randomizer.nextInt(T.size()));
				j = -1;
				while (j!=i){
					j = T.get(randomizer.nextInt(T.size()));
				}
			}
		}else if (tipo_random==1){
			//selecciono primer par i,j in T que cumpla mij>0
			for (Map.Entry<Pair<Integer, Integer>, Integer> entry : m_ij.entrySet()) {
				Pair<Integer, Integer> pair = entry.getKey();
			    int value = entry.getValue();			    
			    if (value>0){
			    	return pair;
			    }
			}
			return null;
		}
		return null;
	}
}


