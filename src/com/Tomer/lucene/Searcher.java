package com.Tomer.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Searcher {
	IndexSearcher indexSearcher;
	QueryParser queryParser;
	Query query;
	
	QueryParser booleanQueryParser;
	BooleanQuery booleanQuery;

	public Searcher(String indexDirectoryPath) throws IOException {
		Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath).toPath());
		DirectoryReader dr = DirectoryReader.open(indexDirectory);
		indexSearcher = new IndexSearcher(dr);
		String[] fieldArray = { LuceneConstants.TITLE, LuceneConstants.CONTENTS, LuceneConstants.PG_TITLE 
				, LuceneConstants.SECOND_TITLE , LuceneConstants.CAPTION };
		String[] booleanFieldArray = { LuceneConstants.TITLE, LuceneConstants.PG_TITLE 
				, LuceneConstants.SECOND_TITLE , LuceneConstants.CAPTION };
		queryParser = new MultiFieldQueryParser(fieldArray, new EnglishAnalyzer());
		booleanQueryParser = new MultiFieldQueryParser(booleanFieldArray, new EnglishAnalyzer());
		//booleanQueryParser.setDefaultOperator(Operator.AND);
	}

	public TopDocs search(String searchQuery) throws IOException, ParseException {
		query = queryParser.parse(searchQuery);
//		query = booleanQueryParser.parse(searchQuery);
//		System.out.println(query);
//		booleanQuery = new BooleanQuery.Builder()
//				.add(new BooleanClause(query,BooleanClause.Occur.MUST))
//				.build();
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}

	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
		return indexSearcher.doc(scoreDoc.doc);
	}

	public void close() throws IOException {
		// to-do: find proper close method for the searcher
		// indexSearcher.close();
	}
}