import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import algorithms.DijkstraShortestPathAlg;
import algorithms.YenTopKShortestPathsAlg;
import model.*;
import model.abstracts.BaseVertex;

public class mor {

	public static void main(String[] args) throws IOException {
		System.out.println("Metaheur�sticas y Optimizaci�n sobre Redes - 2015\n");
		
		System.out.println("Grafos disponibles: ");
		System.out.println("1: 7 nodos - 34 aristas - costos 1.");
		System.out.println("2: 4 nodos - 12 aristas - costos 1.");
		System.out.println("3: 10 nodos - 90 aristas - costos 1.");
		System.out.println("4: 7 nodos - 34 aristas - costos variables.");
		System.out.println("5: 4 nodos - 12 aristas - costos variables.");
		System.out.println("6: 10 nodos - 90 aristas - costos variables.");
		
		System.out.println("\nIngrese el n�mero del grafo:  ");
		System.out.print(">  ");
		BufferedReader buffer_read = new BufferedReader(new InputStreamReader(System.in));
		String nro_grafo = buffer_read.readLine();
		
		System.out.println("Nro. de iteraciones para GRASP:  ");
		System.out.print(">  ");
		buffer_read = new BufferedReader(new InputStreamReader(System.in));

		int max_iter_GRASP = Integer.parseInt(buffer_read.readLine());
		
		System.out.println("Nro. de iteraciones para la CI:  ");
		System.out.print(">  ");
		buffer_read = new BufferedReader(new InputStreamReader(System.in));
		int max_iter_CI = Integer.parseInt(buffer_read.readLine());
		
		System.out.println("Cargo grafo inicial n�mero "+nro_grafo+"...");
		//Cargo grafo desde archivo
		Graph G = new Graph("data/test_mor_"+nro_grafo);
		//Graph G = new Graph("data/c03.stp");
		//G.export_to_file("data/salidas/G_"+nro_grafo+".txt");
		//G.export_to_file("data/salidas/G_c03.stp.txt");

		Graph BestSolutionFound = null;
		
		long startTime = System.currentTimeMillis();
		
		for (int i=1; i<=max_iter_GRASP; i++){			
			//System.out.println("Construyo solución inicial...");		
			Graph InitialSolution = construir_solucion_inicial(G,max_iter_CI);
			//InitialSolution.export_to_file("data/salidas/InitialSolution_c03.stp.txt");
			if (InitialSolution==null) {
				P_ij.clear();
				continue;
			}
			//InitialSolution.export_to_file("data/salidas/InitialSolution_"+nro_grafo+"_"+i+".txt");	
			if (BestSolutionFound==null){
				BestSolutionFound = InitialSolution.copy_of_graph();
			}

			//System.out.println("Iniciando b�squeda local...");
			Graph LocalSolution = busqueda_local(G, InitialSolution);
			//LocalSolution.export_to_file("data/salidas/LocalSolution_"+nro_grafo+"_"+i+".txt");

			//comparo los costos de BestSolutionFound y LocalSolution			
			if (LocalSolution.costo_grafo()<BestSolutionFound.costo_grafo()){			
				BestSolutionFound = LocalSolution.copy_of_graph();
			}		
			
			P_ij.clear();
		}
		long endTime = System.currentTimeMillis();
		
		long totalTime = endTime - startTime; //debemos restarle los tiempos que produce generar los archivos
		System.out.println("Tiempo de ejecución: " + totalTime + "ms.");
		System.out.println("Fin.");	
		BestSolutionFound.export_to_file("data/salidas/BestSolutionFound_"+nro_grafo+".txt");
	}

	private static Map<Pair<Integer,Integer>,List<Path>> P_ij = new HashMap<Pair<Integer,Integer>, List<Path>>();

	private static List<Path> key_path_utilizados = new ArrayList<Path>();

	private static Graph construir_solucion_inicial (Graph G, int max_iter_CI){
		//Cargo nodos terminales (después deberíamos cargarlos desde un archivo)(por ahora harcodeado)
		List<Integer> T = G.getNodos_terminales();

		Map<Pair<Integer, Integer>, Integer> m_ij = new HashMap<Pair<Integer,Integer>, Integer>();

		for (int i = 0; i<T.size(); i++){
			for (int j = i+1; j<T.size(); j++){
				m_ij.put(new Pair<Integer, Integer>(T.get(i),T.get(j)),3);			
			}
		}

		//Creo grafo Gsol cuyos nodos son los terminales y no tiene aristas		
		Graph Gsol = new Graph();
		Gsol.set_vertex_to_graph(T);	

		//seleccionar randomicamente i,j in TxT /mij>0
		Pair<Integer,Integer> random_ij = get_random_ij(m_ij,T);

		
		int iter = 0;
		while ((random_ij!=null)/*&& (iter<max_iter_CI)*/){ //mientras exista un (i,j) in TxT /mij>0
			Graph H = G.copy_of_graph();
			if (P_ij.get(random_ij)!=null){
				for (Path p : P_ij.get(random_ij)){
					H = H.grafo_menos_camino(p);
				}
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

				for (Path p : yens_path){ //ROBLEDO ARREGLAR FERNANDA TO DO ESTO NO ESTA BIEN, NO ES POR CANTIDAD DE VERTICES ES POR COSTO DE ARISTAS!!!!
					int p_size = p.get_vertices().size();
					if (p_size<min_size){
						min_size = p_size;
						shortest_path = p;
					}
				}

				Vertex i = (Vertex) H.get_vertex(random_ij.first());
				Vertex j = (Vertex) H.get_vertex(random_ij.second());

				Pair<Integer, Integer> pair_ij = new Pair<Integer, Integer>(i.get_id(),j.get_id());

				int old_mij = m_ij.get(pair_ij);
				System.out.println(pair_ij.first()+","+pair_ij.second()+"-----"+m_ij.get(pair_ij));
				if (old_mij-1 <= 0){
					m_ij.remove(pair_ij);
				}else {
					m_ij.put(pair_ij,old_mij-1);
				}
				
				

				List<Path> disjoint_node_path_list = P_ij.get(pair_ij);
				if (disjoint_node_path_list==null){
					disjoint_node_path_list = new ArrayList<Path>();
				}

				if (shortest_path.get_weight()==0){
					disjoint_node_path_list.add(shortest_path);					
				}else{
					Random randomizer = new Random();					
					Path p_random = null;
					if (yens_path.size()==1){
						p_random = yens_path.get(0);
					}else {
						p_random = yens_path.get(randomizer.nextInt(yens_path.size()));
					}
					disjoint_node_path_list.add(p_random);		
					Gsol = Gsol.grafo_mas_camino(p_random, G);
				}

				P_ij.put(pair_ij, new ArrayList<Path>(disjoint_node_path_list));
			}else{
				System.out.println("No me diste caminos Yen");
				System.out.println("random_ij es " + random_ij.first() + " - " + random_ij.second());
				//System.out.println("Aristas de H: " + H.)
				return null;
			}

			iter++;
			random_ij = get_random_ij(m_ij,T);
		}		
		return Gsol;
	}

	private static Pair<Integer,Integer> get_random_ij (Map<Pair<Integer, Integer>, Integer> m_ij, List<Integer> T){		
		//selecciono primer par i,j in T que cumpla mij>0
		if ((m_ij != null) && (m_ij.size()>0)){
			 Entry<Pair<Integer, Integer>, Integer> entry=m_ij.entrySet().iterator().next();
			 Pair<Integer,Integer> key= entry.getKey();
			 Integer value=entry.getValue();
			System.out.println("pair (" + key.first() +", " + key.second()+") mij:" + value);
			
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
			//System.out.println("random_ij: "+pair.first()+","+pair.second());
			return pair;
		}else {
			System.out.println("No hay más mij!!! :)");
		}
		return null;		
	}

	private static Graph busqueda_local(Graph G, Graph InitialSolution){
		Graph best_Gsol = InitialSolution.copy_of_graph();
		Graph Gsol = InitialSolution.copy_of_graph();
		int max_iter=50;

		do{			
			//Obtener keypath y la lista de caminos que lo contienen
			Pair<Path,Map<Pair<Integer,Integer>,List<Path>>> lista_caminos_keypath = Gsol.getKeyPathFromGraph(P_ij, key_path_utilizados); //sigue devolviendo keypaths ya usados
			if (lista_caminos_keypath!=null){
				Path p_techo = lista_caminos_keypath.first();

				key_path_utilizados.add(p_techo);
				Map<Pair<Integer,Integer>,List<Path>> par_nodos_caminos = lista_caminos_keypath.second();

				if (par_nodos_caminos==null || par_nodos_caminos.size() == 0){
					return InitialSolution;
				}
				List<BaseVertex> vertices_p_techo = p_techo.get_vertices();
				BaseVertex u = vertices_p_techo.get(0);
				BaseVertex v = vertices_p_techo.get(vertices_p_techo.size()-1);
				Set<Pair<Integer,Integer>> Xp_techo = par_nodos_caminos.keySet();

				//Nos ahorramos Yptecho
				Graph H_techo = G.copy_of_graph();

				//para construir la matriz de costos
				Graph Gsol_menos_p_techo = Gsol.grafo_menos_camino(p_techo);

				Map<Pair<Integer, Integer>, Double> cost_techo = G.get_vertex_pair_weight_index();
				Map<Pair<Integer, Integer>, Double> Gsol_menosp_edges_cost = Gsol_menos_p_techo.get_vertex_pair_weight_index();
				if (Gsol_menosp_edges_cost==null)
					Gsol_menosp_edges_cost = new HashMap<Pair<Integer,Integer>, Double>();
				for (Entry<Pair<Integer, Integer>, Double> entry : Gsol_menosp_edges_cost.entrySet()) {
					Pair<Integer, Integer> pair = entry.getKey();			    
					cost_techo.put(pair, (double) 0);			    
				}

				H_techo.set_vertex_pair_weight_index(cost_techo);

				for(Pair<Integer,Integer> ij : Xp_techo){
					List<Path> caminosP_ij = P_ij.get(ij);
					for (Path camino : caminosP_ij){
						H_techo = H_techo.grafo_menos_camino(camino);
					}

					for (Path camino : par_nodos_caminos.get(ij)){
						H_techo = H_techo.grafo_mas_camino(camino, G); //fijarse que este remove funcione bien
					}

				}

				//obtener p_barra el camino mas corto de u a v en h techo
				DijkstraShortestPathAlg dijkstra = new DijkstraShortestPathAlg(H_techo);
				Path p_barra = dijkstra.get_shortest_path(u, v);
				Gsol_menos_p_techo = Gsol_menos_p_techo.grafo_mas_camino(p_barra, G);
				Gsol = Gsol_menos_p_techo.copy_of_graph();

				//Actualizar los Pij para todo ij perteneciente a x_p_techo
				for (Pair<Integer,Integer> ij:Xp_techo){
					//recorro caminos que devuelve el getkeypathfromgraph 
					List<Path> pij = par_nodos_caminos.get(ij);
					for (Path camino:pij){
						P_ij.get(ij).remove(camino);
						List<BaseVertex> vertices =  camino.get_vertices();
						int indice = vertices.indexOf(u);
						vertices.removeAll(p_techo.get_vertices());
						if (indice>vertices.size()){
							vertices.addAll(p_barra.get_vertices());
						}else {
							vertices.addAll(indice, p_barra.get_vertices());
						}
						camino.setVertexList(vertices);
						P_ij.get(ij).add(camino);
					}
				}				
			}
			max_iter--;
			if (Gsol.costo_grafo()<best_Gsol.costo_grafo()){
				best_Gsol = Gsol.copy_of_graph();
			}				
		}while (max_iter>0);

		return best_Gsol;
	}

}



