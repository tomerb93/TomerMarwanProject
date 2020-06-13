package com.Tomer.lucene;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.File;
import java.io.FileWriter;

public class LuceneTester {
	String indexDir = "C:\\Users\\marwan\\Git\\TomerMarwanProject\\resources\\index";
	String dataDir = "C:\\Users\\marwan\\Git\\TomerMarwanProject\\resources";
	String searchOutputFile = "C:\\Users\\marwan\\Desktop\\tables_redi2_1\\index\\SearchOutput.txt";
	Indexer indexer;
	Searcher searcher;

	public static void main(String[] args) {
		LuceneTester tester;
		try {
			tester = new LuceneTester();
			tester.createIndex();
			tester.search("worlds interested rate tables");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void createIndex() throws IOException, ParseException {
		indexer = new Indexer(indexDir);
		int numIndexed;
		long startTime = System.currentTimeMillis();
		numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
		long endTime = System.currentTimeMillis();
		indexer.close();
		System.out.println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms");
	}

	private void search(String searchQuery) throws IOException, ParseException {
		searcher = new Searcher(indexDir);
		long startTime = System.currentTimeMillis();
		TopDocs hits = searcher.search(searchQuery);
		long endTime = System.currentTimeMillis();

		System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime));
		File file = new File(searchOutputFile);
		FileWriter fr = new FileWriter(file);
		int i = 1;
		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.getDocument(scoreDoc);
			fr.write("1    Q0    " + doc.get(LuceneConstants.TABLE_NAME) + "    " + i + "    " + scoreDoc.score
					+ "    TomerMaryan's Team \n");
			i++;
		}
		fr.close();
		searcher.close();
	}
}