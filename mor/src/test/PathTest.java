package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import model.Path;
import model.Vertex;
import model.abstracts.BaseVertex;

import org.junit.Test;

public class PathTest {

	@Test
	public void testPath_contains_path() {		
		//TEST 1
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
		
		assertTrue(p1.path_contains_path(p2));
		
		
		//TEST 2
		path1 = new ArrayList<BaseVertex>(); //2-3-4-5-6
		for (int i=2; i<7; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			path1.add(v1);
		}
		
		p1 = new Path();
		p1.setVertexList(path1);
		
		path2 = new ArrayList<BaseVertex>(); //3-4-5
		for (int i=3; i<6; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			path2.add(v1);
		}
		
		
		p2 = new Path();
		p2.setVertexList(path2);
		
		assertTrue(p1.path_contains_path(p2));
		
		//TEST3
		path1 = new ArrayList<BaseVertex>(); //2-3
		for (int i=2; i<4; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			path1.add(v1);
		}
		
		p1 = new Path();
		p1.setVertexList(path1);
		
		path2 = new ArrayList<BaseVertex>(); //2-3-4-5
		for (int i=2; i<6; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			path2.add(v1);
		}
		
		
		p2 = new Path();
		p2.setVertexList(path2);
		
		assertFalse(p1.path_contains_path(p2));	
		
		//TEST3
		path1 = new ArrayList<BaseVertex>(); //{}
		
		p1 = new Path();
		p1.setVertexList(path1);
		
		path2 = new ArrayList<BaseVertex>(); //2-3-4-5
		for (int i=2; i<6; i++){
			BaseVertex v1 = new Vertex();
			v1.set_id(i);
			path2.add(v1);
		}
		
		
		p2 = new Path();
		p2.setVertexList(path2);
		
		assertFalse(p1.path_contains_path(p2));
	}

}
