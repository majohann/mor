package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import model.Path;
import model.Vertex;
import model.abstracts.BaseVertex;

import org.junit.Test;

public class BusquedaLocalTest {

	@Test
	public void testCaminoMenosCamino() {
		List<BaseVertex> vertices = new ArrayList<BaseVertex>(); //2-3-4-5-6-7
		for (int i=2; i<8; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			vertices.add(v1);
		}
		
		List<BaseVertex> vertices_a_sacar = new ArrayList<BaseVertex>(); //4-5-6
		for (int i=4; i<7; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			vertices_a_sacar.add(v1);
		}
		
		List<BaseVertex> vertices_a_agregar = new ArrayList<BaseVertex>(); //8-9-10-11
		for (int i=8; i<12; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			vertices_a_agregar.add(v1);
		}
		
		List<BaseVertex> vertices_resultado = new ArrayList<BaseVertex>(); //8-9-10-11
		for (int i=2; i<4; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			vertices_resultado.add(v1);
		}
		for (int i=8; i<12; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			vertices_resultado.add(v1);
		}
		
		BaseVertex v1 = new Vertex();
		v1.set_id(7);
		vertices_resultado.add(v1);
		
		
		BaseVertex u = new Vertex();
		u.set_id(4);
		int indice = vertices.indexOf(u);
		
		/*System.out.print("Lista original: "+write_vertex_list(vertices));
		System.out.println();
		System.out.print("Vertices a sacar: "+write_vertex_list(vertices_a_sacar));
		System.out.println();
		System.out.print("Vertices a agregar: "+write_vertex_list(vertices_a_agregar));
		System.out.println();*/
		vertices.removeAll(vertices_a_sacar);
		vertices.addAll(indice, vertices_a_agregar);
		//System.out.print("Resultado: "+write_vertex_list(vertices));
		
		assertEquals(vertices, vertices_resultado);
		
	}
	
	private String write_vertex_list (List<BaseVertex> list){
		String text = "";
		for (BaseVertex v: list){
			text += v.get_id()+" ";
		}
		return text;
	}
	
	@Test
	public void test_ListPath_menos_path() {
		List<Path> list_path = new ArrayList<Path>();
		List<BaseVertex> vertices_path_1 = new ArrayList<BaseVertex>(); //2-3-4-5-6-7
		for (int i=1; i<9; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			vertices_path_1.add(v1);
		}
		Path path1 = new Path();
		path1.setVertexList(vertices_path_1);
		list_path.add(path1);
		
		List<BaseVertex> vertices_path_2 = new ArrayList<BaseVertex>(); //4-5-6-7
		for (int i=4; i<8; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			vertices_path_2.add(v1);
		}
		
		Path path2 = new Path();
		path2.setVertexList(vertices_path_2);
		list_path.add(path2);
		
		List<BaseVertex> vertices_path_3 = new ArrayList<BaseVertex>(); //1-2-3
		for (int i=1; i<4; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			vertices_path_3.add(v1);
		}
		
		Path path3 = new Path();
		path3.setVertexList(vertices_path_3);
		list_path.add(path3);
		
		List<Path> resultado = new ArrayList<Path>();
		resultado.add(path1);
		resultado.add(path3);
		
		
		String p = "Paths de la lista original: " ;
		for (Path path: list_path){
			p += path.toString() + " ";			
		}
		System.out.println(p);
		
		list_path.remove(path2);
		
		p = "Paths de la lista resultado: " ;
		for (Path path: list_path){
			p += path.toString() + " ";			
		}
		System.out.println(p);
		
		int i=0;
		for (Path res : list_path){
			assertEquals(res, resultado.get(i));
			i++;
		}
		
	}
	
}
