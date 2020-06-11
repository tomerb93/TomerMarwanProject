package com.Tomer.lucene;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.NonReadableChannelException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.document.TextField;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONString;







public class Indexer {
	private IndexWriter writer;

	@SuppressWarnings("deprecation")
	public Indexer(String indexDirectoryPath) throws IOException {
		// this directory will contain the indexes
		
		Analyzer enAnalyzer = new EnglishAnalyzer();
		Similarity tfidfSim = new ClassicSimilarity();
		IndexWriterConfig conf = new IndexWriterConfig(enAnalyzer);
		conf.setSimilarity(tfidfSim);
		Directory dir = FSDirectory.open(new File(indexDirectoryPath).toPath());
		//incremental indexing
		//conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
		conf.setOpenMode(OpenMode.CREATE);
		// create the indexer
		writer = new IndexWriter(dir, conf);
		
	}

	public void close() throws CorruptIndexException, IOException {
		writer.close();
	}

	private Document getDocument(File file) throws IOException {
		Document document = new Document();
		
		document.add(new StringField(LuceneConstants.FILE_NAME, file.getName(), Store.YES));
		document.add(new TextField(LuceneConstants.CONTENTS, new FileReader(file)));
		document.add(new StringField(LuceneConstants.FILE_PATH, file.getCanonicalPath(), Store.YES));
		// index file path
		return document;
	}

	private void indexFile(File file) throws IOException {
		System.out.println("Indexing " + file.getCanonicalPath());
		Document document = getDocument(file);
		writer.addDocument(document);
	}

	public int createIndex(String dataDirPath, FileFilter filter) throws IOException {
		// get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();
		for (File file : files) {
			System.out.println("names: " + file.getName());
			if (file.getName().startsWith("table1.txt")) {
				
				//continue;
			}
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				System.out.println("try: " + new FileReader(file).toString());
				indexFile(file);
			} 
			else if (file.getName().endsWith("json")) {
				try {
					System.out.println("inside try");
					JSONArray arr = new JSONArray((file));
					System.out.println("arr"+arr);
					for (int i = 0; i < arr.length(); i++) {
					    String text = arr.getJSONObject(i).toString();
					    System.out.println(text);
					    //doc.add(new TextField("contents", text), Store.YES));
					}
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: handle exception
				}
				
			}
		}
		return writer.getDocStats().numDocs;
	}
}