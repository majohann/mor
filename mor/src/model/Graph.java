/*
 *
 * Copyright (c) 2004-2008 Arizona State University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ARIZONA STATE UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL ARIZONA STATE UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import model.abstracts.BaseGraph;
import model.abstracts.BaseVertex;
import algorithms.YenTopKShortestPathsAlg;

/** majo
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 783 $
 * @latest $Date: 2009-06-19 12:19:27 -0700 (Fri, 19 Jun 2009) $
 */
public class Graph implements BaseGraph
{
	public final static double DISCONNECTED = Double.MAX_VALUE;
	
	// index of fan-outs of one vertex
	protected Map<Integer, Set<BaseVertex>> _fanout_vertices_index =
		new HashMap<Integer, Set<BaseVertex>>();
	
	// index for fan-ins of one vertex
	protected Map<Integer, Set<BaseVertex>> _fanin_vertices_index =
		new HashMap<Integer, Set<BaseVertex>>();
	
	// index for edge weights in the graph
	protected Map<Pair<Integer, Integer>, Double> _vertex_pair_weight_index = 
		new HashMap<Pair<Integer,Integer>, Double>();
	
	// index for vertices in the graph
	protected Map<Integer, BaseVertex> _id_vertex_index = 
		new HashMap<Integer, BaseVertex>();
	
	// list of vertices in the graph 
	protected List<BaseVertex> _vertex_list = new Vector<BaseVertex>();
	
	// the number of vertices in the graph
	protected int _vertex_num = 0;
	
	// the number of arcs in the graph
	protected int _edge_num = 0;
	
	// nodos terminales de la soluci�n
	private List<Integer> nodos_terminales = new ArrayList<Integer>();	
	
	/**
	 * Constructor 1 
	 * @param data_file_name
	 */
	public Graph(final String data_file_name)
	{
		import_from_file(data_file_name);
	}
	
	/**
	 * Constructor 2
	 * 
	 * @param graph
	 */
	public Graph(final Graph graph_)
	{
		_vertex_num = graph_._vertex_num;
		_edge_num = graph_._edge_num;
		_vertex_list.addAll(graph_._vertex_list);
		_id_vertex_index.putAll(graph_._id_vertex_index);
		_fanin_vertices_index.putAll(graph_._fanin_vertices_index);
		_fanout_vertices_index.putAll(graph_._fanout_vertices_index);
		_vertex_pair_weight_index.putAll(graph_._vertex_pair_weight_index);
	}
	
	/**
	 * Default constructor 
	 */
	public Graph(){};
	
	/**
	 * Clear members of the graph.
	 */
	public void clear()
	{
		Vertex.reset();
		_vertex_num = 0;
		_edge_num = 0; 
		_vertex_list.clear();
		_id_vertex_index.clear();
		_fanin_vertices_index.clear();
		_fanout_vertices_index.clear();
		_vertex_pair_weight_index.clear();
	//	_vertex_pair_mij_index.clear();
	}
	
	/**
	 * There is a requirement for the input graph. 
	 * The ids of vertices must be consecutive. 
	 *  
	 * @param data_file_name
	 */
	public void import_from_file(final String data_file_name)
	{
		// 0. Clear the variables 
		clear();
		
		try
		{
			// 1. read the file and put the content in the buffer
			FileReader input = new FileReader(data_file_name);
			BufferedReader bufRead = new BufferedReader(input);

			boolean is_first_line = true;
			String line; 	// String that holds current file line
			
			// 2. Read first line
			line = bufRead.readLine();
			while(line != null)
			{
				// 2.1 skip the empty line
				if(line.trim().equals("")) 
				{
					line = bufRead.readLine();
					continue;
				}
				
				// 2.2 generate nodes and edges for the graph
				if(is_first_line)
				{
					//2.2.1 obtain the number of nodes in the graph 
					
					is_first_line = false;
					_vertex_num = Integer.parseInt(line.trim());
					for(int i=0; i<_vertex_num; ++i)
					{
						BaseVertex vertex = new Vertex();
						vertex.set_terminales(false);
						_vertex_list.add(vertex);
						_id_vertex_index.put(vertex.get_id(), vertex);
					}
					
				}else
				{
					//2.2.2 find a new edge and put it in the graph  
					String[] str_list = line.trim().split("\\s");
					try {
						int start_vertex_id = Integer.parseInt(str_list[0]);
						int end_vertex_id = Integer.parseInt(str_list[1]);
						double weight = Double.parseDouble(str_list[2]);
						add_edge(start_vertex_id, end_vertex_id, weight);
					} catch (NumberFormatException e){
						//encontro a los terminales
						for (int i = 1; i<str_list.length; i++){
							nodos_terminales.add(Integer.parseInt(str_list[i]));
						}
					}
				}
				//
				line = bufRead.readLine();
			}
			bufRead.close();

		}catch (IOException e)
		{
			// If another exception is generated, print a stack trace
			e.printStackTrace();
		}
	}

	/**
	 * Note that this may not be used externally, because some other members in the class
	 * should be updated at the same time. 
	 * 
	 * @param start_vertex_id
	 * @param end_vertex_id
	 * @param weight
	 */
	protected void add_edge(int start_vertex_id, int end_vertex_id, double weight)
	{
		// actually, we should make sure all vertices ids must be correct. 
		if(!_id_vertex_index.containsKey(start_vertex_id)
		|| !_id_vertex_index.containsKey(end_vertex_id) 
		|| start_vertex_id == end_vertex_id)
		{
			throw new IllegalArgumentException("The edge from "+start_vertex_id
					+" to "+end_vertex_id+" does not exist in the graph.");
		}
		
		// update the adjacent-list of the graph
		Set<BaseVertex> fanout_vertex_set = new HashSet<BaseVertex>();
		if(_fanout_vertices_index.containsKey(start_vertex_id))
		{
			fanout_vertex_set = _fanout_vertices_index.get(start_vertex_id);
		}
		fanout_vertex_set.add(_id_vertex_index.get(end_vertex_id));
		_fanout_vertices_index.put(start_vertex_id, fanout_vertex_set);
		
		//
		Set<BaseVertex> fanin_vertex_set = new HashSet<BaseVertex>();
		if(_fanin_vertices_index.containsKey(end_vertex_id))
		{
			fanin_vertex_set = _fanin_vertices_index.get(end_vertex_id);
		}
		fanin_vertex_set.add(_id_vertex_index.get(start_vertex_id));
		_fanin_vertices_index.put(end_vertex_id, fanin_vertex_set);

		// store the new edge 
		_vertex_pair_weight_index.put(
				new Pair<Integer, Integer>(start_vertex_id, end_vertex_id), 
				weight);
		
		//Inicializo mij=3
		/*_vertex_pair_mij_index.put(new Pair<Integer, Integer>(start_vertex_id, end_vertex_id), 
				3);*/
		++_edge_num;
	}
	
	/**
	 * Store the graph information into a file. 
	 * 
	 * @param file_name
	 */
	public void export_to_file(final String file_name)
	{
		//1. prepare the text to export
		StringBuffer sb = new StringBuffer();
		sb.append(_vertex_num+"\n\n");
		for(Pair<Integer, Integer> cur_edge_pair : _vertex_pair_weight_index.keySet())
		{
			int starting_pt_id = cur_edge_pair.first();
			int ending_pt_id = cur_edge_pair.second();
			double weight = _vertex_pair_weight_index.get(cur_edge_pair);
			sb.append(starting_pt_id+"	"+ending_pt_id+"	"+weight+"\n");
		}
		//2. open the file and put the data into the file. 
		Writer output = null;
		try {
			// use buffering
			// FileWriter always assumes default encoding is OK!
			output = new BufferedWriter(new FileWriter(new File(file_name)));
			output.write(sb.toString());
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}catch(IOException e)
		{
			e.printStackTrace();
		}finally {
			// flush and close both "output" and its underlying FileWriter
			try
			{
				if (output != null) output.close();
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.asu.emit.qyan.alg.model.abstracts.BaseGraph#get_adjacent_vertices(edu.asu.emit.qyan.alg.model.abstracts.BaseVertex)
	 */
	public Set<BaseVertex> get_adjacent_vertices(BaseVertex vertex)
	{
		return _fanout_vertices_index.containsKey(vertex.get_id()) 
				? _fanout_vertices_index.get(vertex.get_id()) 
				: new HashSet<BaseVertex>();
	}

	/* (non-Javadoc)
	 * @see edu.asu.emit.qyan.alg.model.abstracts.BaseGraph#get_precedent_vertices(edu.asu.emit.qyan.alg.model.abstracts.BaseVertex)
	 */
	public Set<BaseVertex> get_precedent_vertices(BaseVertex vertex)
	{
		return _fanin_vertices_index.containsKey(vertex.get_id()) 
				? _fanin_vertices_index.get(vertex.get_id()) 
				: new HashSet<BaseVertex>();
	}
	
	/* (non-Javadoc)
	 * @see edu.asu.emit.qyan.alg.model.abstracts.BaseGraph#get_edge_weight(edu.asu.emit.qyan.alg.model.abstracts.BaseVertex, edu.asu.emit.qyan.alg.model.abstracts.BaseVertex)
	 */
	public double get_edge_weight(BaseVertex source, BaseVertex sink)
	{
		return _vertex_pair_weight_index.containsKey(
					new Pair<Integer, Integer>(source.get_id(), sink.get_id()))? 
							_vertex_pair_weight_index.get(
									new Pair<Integer, Integer>(source.get_id(), sink.get_id())) 
						  : DISCONNECTED;
	}

	/**
	 * Set the number of vertices in the graph
	 * @param num
	 */
	public void set_vertex_num(int num)
	{
		_vertex_num = num;
	}
	
	/**
	 * Return the vertex list in the graph.
	 */
	public List<BaseVertex> get_vertex_list()
	{
		return _vertex_list;
	}
	
	/**
	 * Get the vertex with the input id.
	 * 
	 * @param id
	 * @return
	 */
	public BaseVertex get_vertex(int id)
	{
		return _id_vertex_index.get(id);
	}
	
	
	public void set_vertex_to_graph(List<Integer> vertexs){
		clear();
		_vertex_num = vertexs.size();		
		for (int id : vertexs){
			BaseVertex vertex = new Vertex();
			vertex.set_id(id);
			vertex.set_terminales(true);
			_vertex_list.add(vertex);
			_id_vertex_index.put(vertex.get_id(), vertex);
		}
	}
	
	public int get_vertex_num(){
		return _vertex_num;
		
	}
	
	public Graph copy_of_graph (){
		Graph copy = new Graph();
		copy._vertex_num = _vertex_num;
		copy._edge_num = _edge_num;
		copy._vertex_list = new ArrayList<BaseVertex>(_vertex_list);
		copy._fanin_vertices_index = new HashMap<Integer, Set<BaseVertex>>(_fanin_vertices_index);
		copy._fanout_vertices_index = new HashMap<Integer, Set<BaseVertex>>(_fanout_vertices_index);
		copy._id_vertex_index = new HashMap<Integer, BaseVertex>(_id_vertex_index);
		copy._vertex_pair_weight_index = new HashMap<Pair<Integer,Integer>, Double>(_vertex_pair_weight_index);
		
		return copy;
	}
	
	public Graph grafo_menos_camino (Path p){
		Graph H = copy_of_graph();
		List<BaseVertex> vertices_path = p._vertex_list;
		if ((vertices_path!=null) && (vertices_path.size()>1)){
			int indice_lista = 0;
			int i = 0;
			int j = 0;
			while (indice_lista+1<vertices_path.size()){
				i = vertices_path.get(indice_lista).get_id();
				j = vertices_path.get(indice_lista+1).get_id();
				
				//elimino el j de los vertices salientes y entrantes de i
				Set<BaseVertex> entrantes_i = H._fanin_vertices_index.get(i);
				Set<BaseVertex> salientes_i = H._fanin_vertices_index.get(i);
				
				BaseVertex vertex_j = vertices_path.get(indice_lista+1);
				entrantes_i.remove(vertex_j);
				salientes_i.remove(vertex_j);
				
				H._fanin_vertices_index.put(i, entrantes_i);
				H._fanout_vertices_index.put(i, salientes_i);
				
				//elimino el j de los vertices salientes y entrantes de i
				Set<BaseVertex> entrantes_j = H._fanin_vertices_index.get(j);
				Set<BaseVertex> salientes_j = H._fanin_vertices_index.get(j);
				
				BaseVertex vertex_i = vertices_path.get(indice_lista);
				entrantes_j.remove(vertex_i);
				salientes_j.remove(vertex_i);
				
				H._edge_num = H._edge_num -2;
								
				H._fanin_vertices_index.put(j, entrantes_j);
				H._fanout_vertices_index.put(j, salientes_j);
				indice_lista++;			
			}
			
			//AGREGAR COSTOS
			Pair <Integer, Integer> pair_ij = new Pair<Integer,Integer>(i,j);
			Pair <Integer, Integer> pair_ji = new Pair<Integer,Integer>(j,i);
			H._vertex_pair_weight_index.remove(pair_ij);
			H._vertex_pair_weight_index.remove(pair_ji);
			
		}
		return H;
	}
	
	
	public Graph grafo_mas_camino (Path p, Graph G){
		Graph Gsol = copy_of_graph();
		List<BaseVertex> vertices_path = p._vertex_list;
		if ((vertices_path!=null) && (vertices_path.size()>1)){
			int indice_lista = 0;
			int i = 0;
			int j = 0;
			while (indice_lista+1<vertices_path.size()){
				i = vertices_path.get(indice_lista).get_id();
				j = vertices_path.get(indice_lista+1).get_id();
				
				//ME FIJO SI DEBO AGREGAR V�RTICES NUEVOS A GSol
				if (Gsol._id_vertex_index.get(i)==null){ //el v�rtice i no existe
					BaseVertex v = vertices_path.get(indice_lista);
					v.set_terminales(false);
					Gsol._id_vertex_index.put(i, v);
					Gsol._vertex_list.add(v);
					Gsol._vertex_num++;
				}				
				if (Gsol._id_vertex_index.get(j)==null){ //el v�rtice j no existe
					BaseVertex v = vertices_path.get(indice_lista+1);
					v.set_terminales(false);
					Gsol._id_vertex_index.put(j, v);
					Gsol._vertex_list.add(v);
					Gsol._vertex_num++;
				}
				
				
				//AGREGAR NODOS ENTRANTES Y SALIENTES				
				//agregar nodos entrantes y salientes a i
				Set<BaseVertex> entrantes_i = Gsol._fanin_vertices_index.get(i);
				Set<BaseVertex> saliente_i = Gsol._fanout_vertices_index.get(i);
				
				if (entrantes_i==null){ //Gsol es vac�o
					entrantes_i = new HashSet<BaseVertex>();			
					
					//nuestro grafo no es dirigido por lo cual entrantes_i = salientes_i
					saliente_i = new HashSet<BaseVertex>();
				}
				
				//agregar nodos entrantes y salientes a j
				Set<BaseVertex> entrantes_j = Gsol._fanin_vertices_index.get(j);
				Set<BaseVertex> saliente_j = Gsol._fanout_vertices_index.get(j);
				if (entrantes_j==null){ //Gsol es vac�o
					entrantes_j = new HashSet<BaseVertex>();			
					
					//nuestro grafo no es dirigido por lo cual entrantes_i = salientes_i
					saliente_j = new HashSet<BaseVertex>();				
				}
				
				if (!entrantes_i.contains(vertices_path.get(indice_lista+1))){
					entrantes_i.add(vertices_path.get(indice_lista+1)); //agrego vertice j
					saliente_i.add(vertices_path.get(indice_lista+1)); //agrego vertice j
					Gsol._fanin_vertices_index.put(i, entrantes_i);
					Gsol._fanout_vertices_index.put(i, saliente_i);
					
					entrantes_j.add(vertices_path.get(indice_lista)); //agrego vertice i
					saliente_j.add(vertices_path.get(indice_lista)); //agrego vertice i
					Gsol._fanin_vertices_index.put(j, entrantes_j);
					Gsol._fanout_vertices_index.put(j, saliente_j);
					
					Gsol._edge_num = Gsol._edge_num + 2;
				}						
				
				//AGREGAR COSTOS
				Pair <Integer, Integer> pair_ij = new Pair<Integer,Integer>(i,j);
				Pair <Integer, Integer> pair_ji = new Pair<Integer,Integer>(j,i);
				double cost = G.get_edge_weight(vertices_path.get(indice_lista), vertices_path.get(indice_lista+1));
				Gsol._vertex_pair_weight_index.put(pair_ij , cost);
				Gsol._vertex_pair_weight_index.put(pair_ji , cost);
				
				
				indice_lista ++;
			}
		}
		return Gsol;
	}

		
	public Map<Pair<Integer, Integer>, Double> get_vertex_pair_weight_index() {
		return _vertex_pair_weight_index;
	}

	public void set_vertex_pair_weight_index(
			Map<Pair<Integer, Integer>, Double> _vertex_pair_weight_index) {
		this._vertex_pair_weight_index = new HashMap<Pair<Integer,Integer>, Double>(_vertex_pair_weight_index);
	}
	
	public boolean isGraphEdge (Pair<Integer,Integer> edge){		
		for (Entry<Pair<Integer, Integer>, Double> entry : _vertex_pair_weight_index.entrySet()) {
			Pair<Integer, Integer> pair = entry.getKey();
		    if ((pair.first()==edge.first()) && (pair.second()==edge.second())){
		    	return true;		    	
		    }
		}
		return false;
	}

	public List<Integer> getNodos_terminales() {
		return nodos_terminales;
	}

	public void setNodos_terminales(List<Integer> nodos_terminales) {
		this.nodos_terminales = nodos_terminales;
	}
	
	public Pair<Path,List<Path>> getKeyPathFromGraph(Map<Pair<Integer,Integer>,List<Path>> P_ij){
		Path keyPath = null;
		List<Path> result = new ArrayList<Path>();
		long seed = System.nanoTime();
		List<Integer> nodosADesordenar = this.nodos_terminales;
		Collections.shuffle(nodosADesordenar, new Random(seed));
		List<Integer> nodosADesordenar2 = this.nodos_terminales;
		Collections.shuffle(nodosADesordenar2, new Random(seed));
		for (int indexTerminal1:nodosADesordenar){
			for (int indexTerminal2:nodosADesordenar2){
				if (indexTerminal1!=indexTerminal2){
					BaseVertex terminal1 = get_vertex(indexTerminal1);
					BaseVertex terminal2 = get_vertex(indexTerminal2);
					Pair <Integer,Integer> pair_ij = new Pair<Integer, Integer>(terminal1.get_id(), terminal2.get_id());
					List<Path> caminos12 = P_ij.get(pair_ij);
					for (Path camino:caminos12){
						if (keyPath==null){
							keyPath = getKeyPath(camino);
							if (keyPath!=null){
								result.add(camino);
							}								
						}
						else{
							if (camino.path_contains_path(keyPath)){
								result.add(camino);
							}
						}
							
					}
				}
			}
		}
		return new Pair<Path,List<Path>>(keyPath,result);
	}
	
	public boolean isKeyNode(BaseVertex vertex){
		if (vertex.isTerminalNode())
			return true;
		Set<BaseVertex> entrantes = this._fanin_vertices_index.get(vertex.get_id());
		return entrantes.size() >2;
	}
	
	public Path getKeyPath(Path camino){
		Path keyPath = new Path();
		Iterator<BaseVertex> it = camino._vertex_list.iterator();
		Boolean first = false;
		BaseVertex v;
		// Find the first key-path starting in the first vertex.
		while(it.hasNext()){
			v = it.next();
			if (first){
				keyPath.push_vertex(v);
				first = false;
			} else if (isKeyNode(v)){
				keyPath.push_vertex(v);
				return keyPath;
			}
		}
		return null;
	}
	
	
}





