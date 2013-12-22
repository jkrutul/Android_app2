package com.example.app_2.utils;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.app_2.App_2;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.models.EdgeModel;

public class DFS {
	 
	public static LinkedList<Long> visited = new LinkedList<Long>();
	public static LinkedList<Long> stack = new LinkedList<Long>();
	public static LinkedList<EdgeModel> edges = new LinkedList<EdgeModel>();

	public static int getElements(Long root) {
		int element_counter = 0;
		Context context = App_2.getAppContext();
		visited.clear();
		stack.clear();
		edges.clear();
		String[] projection = { "i." + ImageContract.Columns._ID };
	    
		do {
			try{stack.removeLast();	
			}catch(NoSuchElementException e){	}
			visited.add(root);
			element_counter++;
			Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI + "/" + root);
			Cursor c = context.getContentResolver().query(uri, projection, null, null, "i." + ImageContract.Columns._ID + " DESC");
			if (c != null) {
				c.moveToFirst();
				while (!c.isAfterLast()) {
					Long child = c.getLong(0);
					if(!visited.contains(child) && !stack.contains(child)){
						stack.add(child);
					}
					edges.add(new EdgeModel(root, child));
					c.moveToNext();
				}
				c.close();
			}
			if(stack.isEmpty())
				break;
			root = stack.getLast();
		} while (true);
		
		// pobieram dla wierzcho�k�w kt�re s� kategoriami list� element�w kt�re na niego wskazuja, a nie s� w li�cie odwiedzonych element�w
		LinkedList<Long> categories = getDistinctRoots(edges);
		LinkedList<EdgeModel> category_to_safe = new LinkedList<EdgeModel>();
		try{
			categories.removeFirst(); // usuwam korze� dlatego �e ju� sprawdzi�em czy na niego co� nie wskazuje
		}catch (NoSuchElementException e) {
			// TODO: handle exception
		}
		for(Long category : categories){
			Uri uri = Uri.parse(ParentsOfImageContract.CONTENT_URI + "/" + category);
			Cursor c = context.getContentResolver().query(uri, projection, null, null, "i." + ImageContract.Columns._ID + " DESC");
			if (c != null) {
				c.moveToFirst();
				while (!c.isAfterLast()) {
					Long parent = c.getLong(0);
					if(!visited.contains(parent)){
						for(EdgeModel em : edges){ // usuwam wi�zania kt�re wskazuj� na t� kategori�, poniewa� przydadz� si� kiedy b�d� chcia� odwiedzi� j� z innej kategoii, edges - zawiera wi�zania do usuni�cia.
							if(em.getParent() == category)
								category_to_safe.add(em);
						}
					}
					c.moveToNext();
				}
				c.close();
			}
		}
		
		for(EdgeModel safe : category_to_safe)
			edges.remove(safe);

		return element_counter;
	}
	

	
	private static LinkedList<Long> getDistinctRoots(LinkedList<EdgeModel> edges){
		LinkedList<Long> roots= new LinkedList<Long>();
		for(EdgeModel em : edges){
			Long parent = em.getParent();
			if(!roots.contains(parent))
				roots.add(parent);
		}
		return roots;
	}
}
