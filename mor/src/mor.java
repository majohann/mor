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
		System.out.println("Metaheurísticas y Optimización sobre Redes - 2015\n");
		
		System.out.println("Cargo grafo inicial...");
		//Cargo grafo desde archivo
		Graph G = new Graph("data/test_mor");
		
		boolean hola =G.isKeyNode(G.get_vertex(1));
		
		G.export_to_file("data/salidas/G_creado.txt");
				
		System.out.println("Construyo solución inicial...");
		Graph Gsol = construir_solucion_inicial(G); 
		
		System.out.println("Fin.");
		
		/*
		List<BaseVertex> path1 = new ArrayList<BaseVertex>(); //2-3-4
		for (int i=2; i<5; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			path1.add(v1);
		}
		
		Path p1 = new Path();
		p1.setVertexList(path1);
		
		List<BaseVertex> path2 = new ArrayList<BaseVertex>(); //2-3-4
		for (int i=2; i<5; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			path2.add(v1);
		}
		
		
		Path p2 = new Path();
		p2.setVertexList(path2);
		
		if (p1.path_contains_path(p2)){
			System.out.println("La contiene señor!!");
		}else{
			System.out.println("negativo-central");
		}
		*/
		
	}
	
	
	private static Graph construir_solucion_inicial (Graph G){
		//Cargo nodos terminales (despuï¿½s deberï¿½amos cargarlos desde un archivo)(por ahora harcodeado)
		List<Integer> T = G.getNodos_terminales();
		
		//al momento de cargar los T debo generar esto (por ahora harcodeado)
		Map<Pair<Integer, Integer>, Integer> m_ij = new HashMap<Pair<Integer,Integer>, Integer>();
				
		for (int i = 0; i<T.size(); i++){
			for (int j = i+1; j<T.size(); j++){
				m_ij.put(new Pair<Integer, Integer>(T.get(i),T.get(j)),3);			
			}
		}
				
		//Creo Pij lista de caminos nodos-disjuntos
		List<Path> Pij = new ArrayList<Path>();
		
		//Creo grafo Gsol cuyos nodos son los terminales y no tiene aristas		
		Graph Gsol = new Graph();
		Gsol.set_vertex_to_graph(T);	
		
		//seleccionar randomicamente i,j in TxT /mij>0
		Pair<Integer,Integer> random_ij = get_random_ij(m_ij,T);
			
		Graph H = G.copy_of_graph();
		H.export_to_file("data/salidas/H_G_copy.txt");
		int iter = 0;
		while ((random_ij!=null) && (iter<30)){ //mientras exista un (i,j) in TxT /mij>0
			for (Path p : Pij){
				H = H.grafo_menos_camino(p);
			}
			
			//H.export_to_file("data/salidas/H_menos_camino_"+iter+".txt");
			
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
				if (old_mij-1 == 0){
					m_ij.remove(new Pair<Integer,Integer>(i.get_id(),j.get_id()));
				}else {
					m_ij.put(new Pair<Integer,Integer>(i.get_id(),j.get_id()),old_mij-1);
				}
				if (shortest_path.get_weight()==0){
					Pij.add(shortest_path);					
				}else{
					Random randomizer = new Random();					
					Path p_random = null;
					if (yens_path.size()==1){
						p_random = yens_path.get(0);
					}else {
						p_random = yens_path.get(randomizer.nextInt(yens_path.size()));
					}
					Pij.add(p_random);		
					Gsol = Gsol.grafo_mas_camino(p_random, G);
					//Gsol.export_to_file("data/salidas/Gsol_iter_"+iter+".txt");
				}
			}	
			
			iter++;
			random_ij = get_random_ij(m_ij,T);
		}
		
		Gsol.export_to_file("data/salidas/G_solucion.txt");
		return Gsol;
	}
	
	private static Pair<Integer,Integer> get_random_ij (Map<Pair<Integer, Integer>, Integer> m_ij, List<Integer> T){		
		//selecciono primer par i,j in T que cumpla mij>0
		if ((m_ij != null) && (m_ij.size()>0)){
			Random randomizer = new Random();					
			
			int mij_size = m_ij.size();
			Pair<Integer, Integer> pair = null;
			List<Pair<Integer, Integer>> lista_de_keys = new ArrayList<Pair<Integer,Integer>>(m_ij.keySet());
			if (mij_size == 1){
				pair = lista_de_keys.get(0);
			}else{
				int random_position = randomizer.nextInt(mij_size);									
				pair = lista_de_keys.get(random_position);
			}
			return pair;
		}
		return null;		
	}
}


