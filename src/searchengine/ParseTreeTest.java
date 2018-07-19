package searchengine;

import org.junit.Test;

import static org.junit.Assert.*;
import static searchengine.Token.Type.*;

public class ParseTreeTest {


    static void printTree(ParseTree tree, int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("\t");
        }
        switch (tree.getType()) {
            case PHRASE:
                System.out.println("\"" + (tree.isNegative() ? "-" : "") + (tree.isPlus() ? "+" : "") + tree.getWord() + "\""); break;
            case WORD:
                System.out.println((tree.isNegative() ? "-" : "") + (tree.isPlus() ? "+" : "") + tree.getWord()); break;
            case AND:
                System.out.println("&"); break;
            case OR:
                System.out.println("|"); break;
        }
        if (tree.getLeft() != null && tree.getLeft().getType() != null) {
            printTree(tree.getLeft(), depth + 1);
        }
        if (tree.getRight() != null && tree.getRight().getType() != null) {
            printTree(tree.getRight(), depth + 1);
        }
    }

    @Test
    public void testParseTreeSingleWord()  {
        String input = "query";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(WORD, tree.getType());
        assertEquals("query", tree.getWord());
        assertNull(tree.getLeft());
        assertNull(tree.getRight());
    }

    @Test
    public void testParseTreeBinaryOperator()  {
        String input = "query1 & query2";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertNull(tree.getWord());
        assertEquals(WORD, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getWord());
        assertEquals(WORD, tree.getRight().getType());
        assertEquals("query2", tree.getRight().getWord());
        assertNull(tree.getLeft().getLeft());
        assertNull(tree.getLeft().getRight());
        assertNull(tree.getRight().getLeft());
        assertNull(tree.getRight().getRight());
    }

    @Test
    public void testParseTreeParenthesizedBinaryAnd()  {
        String input = "(query1 & query2)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals("query1", tree.getLeft().getWord());
        assertEquals("query2", tree.getRight().getWord());
        assertNull(tree.getLeft().getLeft());
        assertNull(tree.getLeft().getRight());
        assertNull(tree.getRight().getLeft());
        assertNull(tree.getRight().getRight());
    }

    @Test
    public void testParseTreeParenthesizedBinaryOr()  {
        String input = "(query1 | query2)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals("query1", tree.getLeft().getWord());
        assertEquals("query2", tree.getRight().getWord());
        assertNull(tree.getLeft().getLeft());
        assertNull(tree.getLeft().getRight());
        assertNull(tree.getRight().getLeft());
        assertNull(tree.getRight().getRight());
    }

    @Test
    public void testParseTreeParenthesizedTernary1()  {
        String input = "query | (query1 | query2)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(WORD, tree.getLeft().getType());
        assertEquals("query", tree.getLeft().getWord());
        assertEquals(OR, tree.getRight().getType());
        assertEquals("query1", tree.getRight().getLeft().getWord());
        assertEquals("query2", tree.getRight().getRight().getWord());
    }

    @Test
    public void testParseTreeParenthesizedTernary2()  {
        String input = "query & (query1 & query2)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals(WORD, tree.getLeft().getType());
        assertEquals("query", tree.getLeft().getWord());
        assertEquals(AND, tree.getRight().getType());
        assertEquals("query1", tree.getRight().getLeft().getWord());
        assertEquals("query2", tree.getRight().getRight().getWord());
    }

    @Test
    public void testParseTreeParenthesizedTernary3()  {
        String input = "(query & (query1 & query2))";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals("query", tree.getLeft().getWord());
        assertNull(tree.getLeft().getLeft());
        assertNull(tree.getLeft().getRight());
        assertEquals(AND, tree.getRight().getType());
        assertEquals("query1", tree.getRight().getLeft().getWord());
        assertEquals("query2", tree.getRight().getRight().getWord());
    }

    @Test
    public void testParseTreeParenthesizedTernary4()  {
        String input = "(query1 & query2) & query3";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertNull(tree.getLeft().getRight().getRight());
        assertEquals("query3", tree.getRight().getWord());
        assertNull(tree.getRight().getRight());
    }

    @Test
    public void testParseTreeParenthesizedTernary5()  {
        String input = "((query1 & query2) & query3)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertEquals("query3", tree.getRight().getWord());
        assertNull(tree.getRight().getLeft());
        assertNull(tree.getRight().getRight());
    }

    @Test
    public void testExcessiveParentheses()  {
        String input = "((((((query1 | query2))) & query3)))";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals(OR, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertEquals("query3", tree.getRight().getWord());
        assertNull(tree.getRight().getLeft());
        assertNull(tree.getRight().getRight());
    }

    @Test
    public void testNegativeWord()  {
        String input = "-query";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(WORD, tree.getType());
        assertEquals("query", tree.getWord());
        assertTrue(tree.isNegative());
    }

    @Test
    public void testNegativeBinary()  {
        String input = "-query | -query2";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals("query", tree.getLeft().getWord());
        assertTrue(tree.getLeft().isNegative());
        assertEquals("query2", tree.getRight().getWord());
        assertTrue(tree.getRight().isNegative());
    }

    @Test
    public void testNegativeBinaryParenthesized()  {
        String input = "(-query | -query2)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals("query", tree.getLeft().getWord());
        assertTrue(tree.getLeft().isNegative());
        assertEquals(tree.getRight().getWord(), "query2");
        assertTrue(tree.getRight().isNegative());
    }

    @Test
    public void testSeparateParentheses()  {
        String input = "(-query1 & query2) | (query3 & -query4)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals(AND, tree.getRight().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertTrue(tree.getLeft().getLeft().isNegative());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertFalse(tree.getLeft().getRight().isNegative());
        assertEquals("query3", tree.getRight().getLeft().getWord());
        assertFalse(tree.getRight().getLeft().isNegative());
        assertEquals("query4", tree.getRight().getRight().getWord());
        assertTrue(tree.getRight().getRight().isNegative());
        assertNull(tree.getLeft().getLeft().getLeft());
        assertNull(tree.getLeft().getLeft().getRight());
        assertNull(tree.getLeft().getRight().getLeft());
        assertNull(tree.getLeft().getRight().getRight());
        assertNull(tree.getRight().getLeft().getLeft());
        assertNull(tree.getRight().getLeft().getRight());
        assertNull(tree.getRight().getRight().getLeft());
        assertNull(tree.getRight().getRight().getRight());

    }

    @Test
    public void testDemorgansLawNegation()  {
        String input = "-(-query1 | query2)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals(tree.getLeft().getWord(), "query1");
        assertFalse(tree.getLeft().isNegative());
        assertEquals(tree.getRight().getWord(), "query2");
        assertTrue(tree.getRight().isNegative());
    }

    @Test
    public void testPlusSign()  {
        String input = "+I";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals("i", tree.getWord());
        assertEquals(WORD, tree.getType());
        assertTrue(tree.isPlus());
    }

    @Test
    public void testPhraseQuery()  {
        String input = "\"This is a phrase with some weird characters: -+()&|.\"";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(PHRASE, tree.getType());
        assertEquals("this is a phrase with some weird characters", tree.getWord());
        assertNull(tree.getLeft());
        assertNull(tree.getRight());
    }

    @Test
    public void testSingleWordPhraseQuery()  {
        String input = "\" Word1 \""; //should be trimmed
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(WORD, tree.getType());
        assertTrue(tree.isPlus());
    }

    @Test
    public void testImplicitAnd()  {
        String input = "word1 word2";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertNull(tree.getWord());
        assertEquals("word1", tree.getLeft().getWord());
        assertEquals(WORD, tree.getLeft().getType());
        assertEquals("word2", tree.getRight().getWord());
        assertEquals(WORD, tree.getRight().getType());
    }

    @Test
    public void testRecursiveImplicitAndWithModifiers()  {
        String input = "-word1 +word2 word3";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertNull(tree.getWord());
        assertEquals("word1", tree.getLeft().getWord());
        assertEquals(WORD, tree.getLeft().getType());
        assertTrue(tree.getLeft().isNegative());
        assertEquals(AND, tree.getRight().getType());
        assertEquals("word2", tree.getRight().getLeft().getWord());
        assertTrue(tree.getRight().getLeft().isPlus());
        assertFalse(tree.getRight().getLeft().isNegative());
        assertEquals("word3", tree.getRight().getRight().getWord());
        assertFalse(tree.getRight().getRight().isNegative());
        assertFalse(tree.getRight().getRight().isPlus());
    }

    @Test
    public void testPriorityRotation()  {
        String input = "query1 & query2 | \"query3 query4\"";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertEquals(PHRASE, tree.getRight().getType());
        assertEquals("query3 query4", tree.getRight().getWord());

    }

    @Test
    public void testRecursivePriorityRotationWithModifiers()  {
        String input = "query1 & query2 & +query3 | -query4";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals(AND, tree.getLeft().getRight().getType());
        assertEquals("query2", tree.getLeft().getRight().getLeft().getWord());
        assertEquals("query3", tree.getLeft().getRight().getRight().getWord());
        assertTrue(tree.getLeft().getRight().getRight().isPlus());
        assertEquals("query4", tree.getRight().getWord());
        assertTrue(tree.getRight().isNegative());
    }

    @Test
    public void testPriorityRotationImplicitAnd()  {
        String input = "query1 query2 | \"query3 query4\"";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertEquals(PHRASE, tree.getRight().getType());
        assertEquals("query3 query4", tree.getRight().getWord());
    }

    @Test
    public void testPriorityRotationDiamondCase()  {
        String input = "query1 query2 | query3 query4";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertEquals(AND, tree.getRight().getType());
        assertEquals("query3", tree.getRight().getLeft().getWord());
        assertEquals("query4", tree.getRight().getRight().getWord());
    }

    @Test
    public void testPriorityRotationWithParenthesizedQuery1()  {
        String input = "query1 & (query2) | \"query3 query4\"";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertEquals(PHRASE, tree.getRight().getType());
        assertEquals("query3 query4", tree.getRight().getWord());
    }

    @Test
    public void testPriorityRotationWithParenthesizedQuery2()  {
        String input = "(query1) query2 | query3";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertEquals(WORD, tree.getRight().getType());
        assertEquals("query3", tree.getRight().getWord());
    }

    @Test
    public void testIllegalParenthesisResolve() {
        String input = "query1 ) query2";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals("query1", tree.getLeft().getWord());
        assertEquals("query2", tree.getRight().getWord());
    }

    @Test
    public void testMutipleIllegalParenthesisResolve() {
        String input = "query1 ))))) query2)) )";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals("query1", tree.getLeft().getWord());
        assertEquals("query2", tree.getRight().getWord());
    }

    @Test
    public void testIllegalParenthesisResolveWithProperParentheses() {
        String input = "query1 ))))) ((query2) +query3) )";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals("query1", tree.getLeft().getWord());
        assertEquals(AND, tree.getRight().getType());
        assertEquals("query2", tree.getRight().getLeft().getWord());
        assertEquals("query3", tree.getRight().getRight().getWord());
        assertTrue(tree.getRight().getRight().isPlus());
    }

    @Test
    public void testMultipleModifiers() {
        String input = "-+I";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertTrue(tree.isNegative());
        assertTrue(tree.isPlus());
    }

    @Test
    public void testExtraneousOperator1() {
        String input = "& query1 & query2";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals("query1", tree.getLeft().getWord());
        assertEquals("query2", tree.getRight().getWord());
        assertNull(tree.getLeft().getLeft());
        assertNull(tree.getLeft().getRight());
        assertNull(tree.getRight().getLeft());
        assertNull(tree.getRight().getRight());
    }


    @Test
    public void testExtraneousOperator2() {
        String input = "| query1 & query2";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals("query1", tree.getLeft().getWord());
        assertEquals("query2", tree.getRight().getWord());
        assertNull(tree.getLeft().getLeft());
        assertNull(tree.getLeft().getRight());
        assertNull(tree.getRight().getLeft());
        assertNull(tree.getRight().getRight());
    }

    @Test
    public void testExtraneousOperator3() {
        String input = "query1 & query2 |";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals(AND, tree.getLeft().getType());
        assertEquals("query1", tree.getLeft().getLeft().getWord());
        assertEquals("query2", tree.getLeft().getRight().getWord());
        assertNull(tree.getRight().getType());
    }

    @Test
    public void testEmptyParentheses() {
        String input = "() | query";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(OR, tree.getType());
        assertEquals("query", tree.getRight().getWord());
    }

    @Test
    public void testExcessiveEmptyParentheses() {
        String input = "query1 & ((())))";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
        assertEquals(AND, tree.getType());
        assertEquals("query1", tree.getLeft().getWord());
    }

    /*@Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidOperatorSequence()  {
        String input = "|&+Word";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidOperatorSequence2()  {
        String input = "Word1 |& Word2";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidOperatorSequence3()  {
        String input = "Word1 )(Word2 Word3)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidEmptyPhrase()  {
        String input = "\"\"";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidUnclosedPhrase1()  {
        String input = "\"";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidUnclosedPhrase2()  {
        String input = "\"SomeTextHere";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidUnequalParentheses()  {
        String input = "((SomeQuery | -SomeOtherQuery)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test
    public void testInvalidModifier1()  {
        String input = "+(SomeQuery | some other query)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidModifier2()  {
        String input = "(SomeQuery -& otherWord)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidModifier3()  {
        String input = "(SomeQuery -)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }

    @Test(expected = QueryEngine.MalformedQueryException.class)
    public void testInvalidModifier4()  {
        String input = "(SomeQuery +)";
        ParseTree tree = ParseTree.fromQuery(new Tokenizer(input));
    }
    */
}