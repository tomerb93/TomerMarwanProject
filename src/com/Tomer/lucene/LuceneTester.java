package com.Tomer.lucene;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.File;
import java.io.FileWriter;

public class LuceneTester {
	String indexDir = "C:\\Dev\\IR-project-files\\Index";
	String dataDir = "C:\\Dev\\IR-project-files\\DataReal";
	String searchOutputFolder = "C:\\Dev\\IR-project-files\\SearchOutput";
	Indexer indexer;
	Searcher searcher;

	public static void main(String[] args) {
		LuceneTester tester;
		try {

			tester = new LuceneTester();
			tester.createIndex();

			for (int i = 0; i < LuceneConstants.QUERIES.length; i++) {
				tester.search(LuceneConstants.QUERIES[i], i + 1);
			}

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

	private void search(String searchQuery, int currQueryID) throws IOException, ParseException {

		searcher = new Searcher(indexDir);

		long startTime = System.currentTimeMillis();
		TopDocs hits = searcher.search(searchQuery);
		long endTime = System.currentTimeMillis();

		System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime));

		File file = new File(searchOutputFolder + "\\searchOutput_" + currQueryID + "_.txt");
		FileWriter fr = new FileWriter(file);
		int i = 1;

		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.getDocument(scoreDoc);
			fr.write(currQueryID + "\tQ0\t" + doc.get(LuceneConstants.TABLE_NAME) + "\t" + i + "\t" + scoreDoc.score
					+ "\tTomerMarwan's Team\n");
			i++;
		}

		fr.close();
	}
}