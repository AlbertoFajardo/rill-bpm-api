/**
(c) Copyright 2008 Hewlett-Packard Development Company, L.P.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.*/

package com.hp.gagawa.java;

public class EscapeText {

	// Comment by MENGRAN at 2012-10-19 for non-used.
	
//	public static final String escapeHTML(String s){
//		StringBuffer sb = new StringBuffer();
//		s = s.replaceAll("&#10;"," ");
//		s = s.replaceAll("&#13;"," ");
//		int n = s.length();
//		for (int i = 0; i < n; i++) {
//			char c = s.charAt(i);
//			switch (c) {
//			case '\n': sb.append(" "); break;
//			case '\r': sb.append(" "); break;
//			case '"': sb.append("&quot;"); break;
//			case '\'':sb.append("&#39;"); break;
//			case '<':sb.append("&lt;"); break;
//			case '>':sb.append("&gt;"); break;
//			case '�': sb.append("&agrave;");break;
//			case '�': sb.append("&Agrave;");break;
//			case '�': sb.append("&acirc;");break;
//			case '�': sb.append("&Acirc;");break;
//			case '�': sb.append("&auml;");break;
//			case '�': sb.append("&Auml;");break;
//			case '�': sb.append("&aring;");break;
//			case '�': sb.append("&Aring;");break;
//			case '�': sb.append("&aelig;");break;
//			case '�': sb.append("&AElig;");break;
//			case '�': sb.append("&ccedil;");break;
//			case '�': sb.append("&Ccedil;");break;
//			case '�': sb.append("&eacute;");break;
//			case '�': sb.append("&Eacute;");break;
//			case '�': sb.append("&egrave;");break;
//			case '�': sb.append("&Egrave;");break;
//			case '�': sb.append("&ecirc;");break;
//			case '�': sb.append("&Ecirc;");break;
//			case '�': sb.append("&euml;");break;
//			case '�': sb.append("&Euml;");break;
//			case '�': sb.append("&iuml;");break;
//			case '�': sb.append("&Iuml;");break;
//			case '�': sb.append("&ocirc;");break;
//			case '�': sb.append("&Ocirc;");break;
//			case '�': sb.append("&ouml;");break;
//			case '�': sb.append("&Ouml;");break;
//			case '�': sb.append("&oslash;");break;
//			case '�': sb.append("&Oslash;");break;
//			case '�': sb.append("&szlig;");break;
//			case '�': sb.append("&ugrave;");break;
//			case '�': sb.append("&Ugrave;");break;         
//			case '�': sb.append("&ucirc;");break;         
//			case '�': sb.append("&Ucirc;");break;
//			case '�': sb.append("&uuml;");break;
//			case '�': sb.append("&Uuml;");break;
//			case '�': sb.append("&reg;");break;         
//			case '�': sb.append("&copy;");break;   
//			case '€': sb.append("&euro;"); break;        
//
//			default:  sb.append(c); break;
//			}
//		}
//		return sb.toString();
//	}
//
//	public static final String escapeHTMLForJSParam(String s){
//		StringBuffer sb = new StringBuffer();
//		s = s.replaceAll("&#10;"," ");
//		s = s.replaceAll("&#13;"," ");
//		int n = s.length();
//		for (int i = 0; i < n; i++) {
//			char c = s.charAt(i);
//			switch (c) {
//			case '\n': sb.append(" "); break;
//			case '\r': sb.append(" "); break;
//			case '"': sb.append("\\&quot;"); break;
//			case '\'':sb.append("\\&#39;"); break;
//			case '<':sb.append("&lt;"); break;
//			case '>':sb.append("&gt;"); break;
//			case '�': sb.append("&agrave;");break;
//			case '�': sb.append("&Agrave;");break;
//			case '�': sb.append("&acirc;");break;
//			case '�': sb.append("&Acirc;");break;
//			case '�': sb.append("&auml;");break;
//			case '�': sb.append("&Auml;");break;
//			case '�': sb.append("&aring;");break;
//			case '�': sb.append("&Aring;");break;
//			case '�': sb.append("&aelig;");break;
//			case '�': sb.append("&AElig;");break;
//			case '�': sb.append("&ccedil;");break;
//			case '�': sb.append("&Ccedil;");break;
//			case '�': sb.append("&eacute;");break;
//			case '�': sb.append("&Eacute;");break;
//			case '�': sb.append("&egrave;");break;
//			case '�': sb.append("&Egrave;");break;
//			case '�': sb.append("&ecirc;");break;
//			case '�': sb.append("&Ecirc;");break;
//			case '�': sb.append("&euml;");break;
//			case '�': sb.append("&Euml;");break;
//			case '�': sb.append("&iuml;");break;
//			case '�': sb.append("&Iuml;");break;
//			case '�': sb.append("&ocirc;");break;
//			case '�': sb.append("&Ocirc;");break;
//			case '�': sb.append("&ouml;");break;
//			case '�': sb.append("&Ouml;");break;
//			case '�': sb.append("&oslash;");break;
//			case '�': sb.append("&Oslash;");break;
//			case '�': sb.append("&szlig;");break;
//			case '�': sb.append("&ugrave;");break;
//			case '�': sb.append("&Ugrave;");break;         
//			case '�': sb.append("&ucirc;");break;         
//			case '�': sb.append("&Ucirc;");break;
//			case '�': sb.append("&uuml;");break;
//			case '�': sb.append("&Uuml;");break;
//			case '�': sb.append("&reg;");break;         
//			case '�': sb.append("&copy;");break;   
//			case '€': sb.append("&euro;"); break;        
//
//			default:  sb.append(c); break;
//			}
//		}
//		return sb.toString();
//	}

}