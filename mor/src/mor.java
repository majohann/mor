import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
		System.out.println("Metaheurísticas y Optimización sobre Redes - 2015\n");

		System.out.println("Los grafos disponibles son los ejecutados en las pruebas detalladas en el informe.");
		System.out.println("Ingrese el número del caso a ejecutar: ");
		System.out.println("1: caso1.");
		System.out.println("2: caso2.");
		System.out.println("3: caso3.");
		System.out.println("4: caso4.");
		System.out.println("5: caso5.");
		System.out.println("6: caso6.");
		System.out.println("7: caso6.");
		System.out.println("8: berlin52.");
		System.out.println("9: brasil58.");

		System.out.print(">  ");
		BufferedReader buffer_read = new BufferedReader(new InputStreamReader(System.in));
		String grafo_seleccionado = buffer_read.readLine();

		System.out.println("Nro. de iteraciones:  ");
		System.out.print(">  ");
		buffer_read = new BufferedReader(new InputStreamReader(System.in));

		int max_iter = Integer.parseInt(buffer_read.readLine());		

		double costo_inicial=0;
		double costo_best=0;

		System.out.println("Cargo grafo inicial número "+grafo_seleccionado+"...");
		System.out.println("");
		//Cargo grafo desde archivo
		if (grafo_seleccionado.equals("8")){
			grafo_seleccionado = "berlin52.stp";
		}else if (grafo_seleccionado.equals("9")){
			grafo_seleccionado = "brasil58.stp";
		}else if (grafo_seleccionado.equals("7")){
			grafo_seleccionado = "p4e100.stp";
		}else{
			grafo_seleccionado = "caso"+grafo_seleccionado;
		}
		
		Graph G = new Graph("data/"+grafo_seleccionado);		
		G.export_to_file("data/salidas/"+grafo_seleccionado+"-"+max_iter+"iter.txt",0,0);

		Graph BestSolutionFound = null;

		long startTime = System.currentTimeMillis();

		for (int i=1; i<=max_iter; i++){			
			System.out.println("Construyo solución inicial...");		
			Graph InitialSolution = construir_solucion_inicial(G,max_iter);
			if (InitialSolution==null) {
				P_ij.clear();
				continue;
			}
			if (BestSolutionFound==null){
				BestSolutionFound = InitialSolution.copy_of_graph();	
				InitialSolution.export_to_file("data/salidas/InitialSolution_"+grafo_seleccionado+grafo_seleccionado+"-"+max_iter+"iter.txt",0,0);
			}
			long middle_time = (System.currentTimeMillis() - startTime)/1000;
			System.out.println(i+") Tiempo solución inicial: " + middle_time + "s.");

			System.out.println("Iniciando búsqueda local...");
			Graph LocalSolution = busqueda_local(G, InitialSolution,max_iter);

			//comparo los costos de BestSolutionFound y LocalSolution			
			if (LocalSolution.costo_grafo()<BestSolutionFound.costo_grafo()){	
				InitialSolution.export_to_file("data/salidas/InitialSolution_"+grafo_seleccionado+grafo_seleccionado+"-"+max_iter+"iter.txt",0,0);
				costo_inicial = InitialSolution.costo_grafo();
				BestSolutionFound = LocalSolution.copy_of_graph();
				costo_best = BestSolutionFound.costo_grafo();
			}		

			P_ij.clear();
		}
		long endTime = System.currentTimeMillis();

		long totalTime = endTime - startTime; //debemos restarle los tiempos que produce generar los archivos
		System.out.println("Tiempo de ejecución: " + totalTime + "ms.");
		System.out.println("Fin.");	
		BestSolutionFound.export_to_file("data/salidas/BestSolutionFound_"+grafo_seleccionado+grafo_seleccionado+"-"+max_iter+"iter.txt",costo_inicial,costo_best);
	}

	private static Map<Pair<Integer,Integer>,List<Path>> P_ij = new HashMap<Pair<Integer,Integer>, List<Path>>();

	private static List<Path> key_path_utilizados = new ArrayList<Path>();

	private static Graph construir_solucion_inicial (Graph G, int max_iter_CI){
		//Cargo nodos terminales (despuÃ©s deberÃ­amos cargarlos desde un archivo)(por ahora harcodeado)
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
				double min_size = Integer.MAX_VALUE;
				Path shortest_path = null; 

				for (Path p : yens_path){ 
					double p_size = p.get_weight();
					if (p_size<min_size){
						min_size = p_size;
						shortest_path = p;
					}
				}

				Vertex i = (Vertex) H.get_vertex(random_ij.first());
				Vertex j = (Vertex) H.get_vertex(random_ij.second());

				Pair<Integer, Integer> pair_ij = new Pair<Integer, Integer>(i.get_id(),j.get_id());

				int old_mij = m_ij.get(pair_ij);
				//System.out.println(pair_ij.first()+","+pair_ij.second()+"-----"+m_ij.get(pair_ij));
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
				System.out.println("El algoritmo de Yen no retornó caminos.");
				return null;
			}

			random_ij = get_random_ij(m_ij,T);
		}		
		return Gsol;
	}

	private static Pair<Integer,Integer> get_random_ij (Map<Pair<Integer, Integer>, Integer> m_ij, List<Integer> T){		
		//selecciono primer par i,j in T que cumpla mij>0
		if ((m_ij != null) && (m_ij.size()>0)){
			Entry<Pair<Integer, Integer>, Integer> entry=m_ij.entrySet().iterator().next();
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
		}else {
			System.out.println("No hay más mij!!! :)");
		}
		return null;		
	}

	private static Graph busqueda_local(Graph G, Graph InitialSolution, int max_iter){
		Graph best_Gsol = InitialSolution.copy_of_graph();
		Graph Gsol = InitialSolution.copy_of_graph();

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

				if (p_barra.get_vertices().get(0)!=p_techo.get_vertices().get(0)){
					p_barra = p_barra.invertirPath();
				}

				Gsol_menos_p_techo = Gsol_menos_p_techo.grafo_mas_camino(p_barra, G);
				Gsol = Gsol_menos_p_techo.copy_of_graph();

				//Actualizar los Pij para todo ij perteneciente a x_p_techo
				for (Pair<Integer,Integer> ij:Xp_techo){
					//recorro caminos que devuelve el getkeypathfromgraph 
					List<Path> pij = par_nodos_caminos.get(ij);

					for (Path camino:pij){
						if (camino.path_contains_path(p_techo)){							
							P_ij.get(ij).remove(camino);
							List<BaseVertex> vertices =  camino.get_vertices();
							List<BaseVertex> vertices_original = new ArrayList<BaseVertex>(camino.get_vertices()); //copia del camino original, usado si no deber realizarse finalmente la sustitucion
							int indice = vertices.indexOf(u);
							vertices.removeAll(p_techo.get_vertices());
							if (indice>vertices.size()){
								vertices.addAll(p_barra.get_vertices());
							}else {
								vertices.addAll(indice, p_barra.get_vertices());
							}

							//Lo que hago acá es ver cuantas ocurrencias hay de cada vertice en el camino 
							//luego de sustituir p_techo por p_barra. Si no hay ciclos se realiza la sustitución
							//si no se sigue con la siguiente iteración.
							Set<BaseVertex> set_vertices = new HashSet<BaseVertex>(vertices);
							boolean realizar_sustitucion = true;
							for (BaseVertex vertex : set_vertices) {
								int cant_ocurrencias = Collections.frequency(vertices, vertex);
								if (cant_ocurrencias>1){
									realizar_sustitucion = false;
								}
							}
							if (realizar_sustitucion){
								camino.setVertexList(vertices);
							}else{
								camino.setVertexList(vertices_original);
							}
							P_ij.get(ij).add(camino);
						}
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
