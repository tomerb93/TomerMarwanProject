package com.Tomer.lucene;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
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
		conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
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
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				indexFile(file);
			}
		}
		return writer.getDocStats().numDocs;
	}
}