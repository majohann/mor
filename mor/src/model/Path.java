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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import model.abstracts.BaseElementWithWeight;
import model.abstracts.BaseVertex;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 673 $
 * @latest $Date: 2009-02-05 01:19:18 -0700 (Thu, 05 Feb 2009) $
 */
public class Path implements BaseElementWithWeight
{
	List<BaseVertex> _vertex_list = new Vector<BaseVertex>();
	double _weight = -1;
	
	public Path(){};
	
	public Path(List<BaseVertex> _vertex_list, double _weight)
	{
		this._vertex_list = _vertex_list;
		this._weight = _weight;
	}

	public double get_weight()
	{
		return _weight;
	}
	
	public void set_weight(double weight)
	{
		_weight = weight;
	}
	
	public List<BaseVertex> get_vertices()
	{
		return _vertex_list;
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object right)
	{
		if(right instanceof Path)
		{
			Path r_path = (Path) right;
			return _vertex_list.equals(r_path._vertex_list);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return _vertex_list.hashCode();
	}
	
	public String toString()
	{
		return _vertex_list.toString()+":"+_weight;
	}
	
	public boolean path_contains_path (Path path){
		List<BaseVertex> p_vertex_list = path._vertex_list;
		if ((_vertex_list!=null) && (_vertex_list.size()>0) && (p_vertex_list!=null) && (p_vertex_list.size()>0)){
			
			int indice_comienzo = 0;
			Iterator<BaseVertex> it = _vertex_list.iterator();
			boolean encontre_comienzo = false;
			while (it.hasNext() && (!encontre_comienzo)){
				BaseVertex v = it.next();
				if (v.get_id() == p_vertex_list.get(0).get_id()){
					encontre_comienzo = true;
				}else{
					indice_comienzo++;
				}
			}
			
			if (encontre_comienzo){
				int indice_p = 0;
				boolean caminos_iguales = true;
				int p_vertex_list_length=p_vertex_list.size();
				for (int indice = indice_comienzo; indice<_vertex_list.size() && caminos_iguales && (indice_p<p_vertex_list_length); indice++){
					if (_vertex_list.get(indice).get_id()==p_vertex_list.get(indice_p).get_id()){
						indice_p++;
					}else{
						caminos_iguales = false;
					}
				}
				if (indice_p<p_vertex_list_length)
					return false;
				return caminos_iguales;
			}
			return false;
			
		}
		return false;
	}
	
	public void setVertexList (List<BaseVertex> lista){
		_vertex_list = new ArrayList<BaseVertex>(lista);
	}
	
	public void push_vertex(BaseVertex v){
		_vertex_list.add(v);
	}
}
