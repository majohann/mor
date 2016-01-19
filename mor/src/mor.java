import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import algorithms.YenTopKShortestPathsAlg;
import model.*;
import model.abstracts.BaseVertex;

public class mor {

	public static void main(String[] args) {
		System.out.println("Metaheurísticas y Optimización sobre Redes - 2015");
		
		//Cargo grafo desde archivo
		Graph G = new Graph("data/test_mor_2");
		G.export_to_file("data/salidas/G_creado.txt");
				
		//Cargo nodos terminales (después deberíamos cargarlos desde un archivo)(por ahora harcodeado)
		List<Integer> T = new ArrayList<Integer>();
		T.add(0);
		T.add(3);
		T.add(5);
		
		//al momento de cargar los T debo generar esto (por ahora harcodeado)
		Map<Pair<Integer, Integer>, Integer> m_ij = new HashMap<Pair<Integer,Integer>, Integer>();
		m_ij.put(new Pair<Integer, Integer>(0,3),3);
		m_ij.put(new Pair<Integer, Integer>(3,5),3);
		//m_ij.put(new Pair<Integer, Integer>(5,1),3);	
		
		//Creo Pij lista de caminos nodos-disjuntos
		List<Path> Pij = new ArrayList<Path>();
		
		//Creo grafo Gsol cuyos nodos son los terminales y no tiene aristas		
		Graph Gsol = new Graph();
		Gsol.set_vertex_to_graph(T);	
		
		//seleccionar randomicamente i,j in TxT /mij>0
		Pair<Integer,Integer> random_ij = get_random_ij(m_ij,T,1);
		
		Map<Pair<Integer, Integer>, Double> costo_techo = new HashMap<Pair<Integer,Integer>, Double>();
		Graph H = G.copy_of_graph();
		H.export_to_file("data/salidas/H_G_copy.txt");
		int iter = 0;
		while ((random_ij!=null) && (iter<30)){ //mientras exista un (i,j) in TxT /mij>0
			for (Path p : Pij){
				H = H.grafo_menos_camino(p);
			}

			Map<Pair<Integer, Integer>, Double> cost_techo = G.get_vertex_pair_weight_index();
			Map<Pair<Integer, Integer>, Double> Gsol_edges_cost = Gsol.get_vertex_pair_weight_index();
			if (Gsol_edges_cost==null)
				Gsol_edges_cost = new HashMap<Pair<Integer,Integer>, Double>();
			for (Entry<Pair<Integer, Integer>, Double> entry : Gsol_edges_cost.entrySet()) {
				Pair<Integer, Integer> pair = entry.getKey();			    
				cost_techo.put(pair, (double) 0);			    
			}
			
			H.set_vertex_pair_weight_index(cost_techo);
			
			//Yen
			YenTopKShortestPathsAlg yen = new YenTopKShortestPathsAlg(H);
			List<Path> yens_path = yen.get_shortest_paths(H.get_vertex(random_ij.first()),H.get_vertex(random_ij.second()), 3);
			
			if ((yens_path!=null)&&(yens_path.size()>0)){
				int min_size = Integer.MAX_VALUE;
				Path shortest_path = null; 
				
				for (Path p : yens_path){
					int p_size = p.get_vertices().size();
					if (p_size<min_size){
						min_size = p_size;
						shortest_path = p;
					}
				}
				
				Vertex i = (Vertex) H.get_vertex(random_ij.first());
				Vertex j = (Vertex) H.get_vertex(random_ij.second());
				int old_mij = m_ij.get(new Pair<Integer,Integer>(i.get_id(),j.get_id()));
				m_ij.put(new Pair<Integer,Integer>(i.get_id(),j.get_id()),old_mij-1);
				if (shortest_path.get_weight()==0){
					Pij.add(shortest_path);					
				}else{
					Random randomizer = new Random();					
					Path p_random = null;
					if (yens_path.size()==1){
						p_random = yens_path.get(0);
					}else {
						p_random = yens_path.get(randomizer.nextInt(yens_path.size()-1));
					}
					Pij.add(p_random);		
					Gsol = Gsol.grafo_mas_camino(p_random, G);
					Gsol.export_to_file("data/salidas/Gsol_iter_"+iter+".txt");
				}
			}	
			
			iter++;
			random_ij = get_random_ij(m_ij,T,1);
		}
		
		Gsol.export_to_file("data/salidas/G_solucion.txt");
		
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


