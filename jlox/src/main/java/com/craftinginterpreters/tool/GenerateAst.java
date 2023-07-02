package com.craftinginterpreters.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateAst {

    private static final String baseName = "Expr";
    private static final String pad1 = "    ";
    private static final String pad2 = pad1 + pad1;
    private static final String pad3 = pad2 + pad1;

    private static final List<Ast> objects = new ArrayList<>() {
        {
            add(new Ast("Binary", List.of(
                    new Param("Expr", "left"),
                    new Param("Token", "operator"),
                    new Param("Expr", "right"))));
            add(new Ast("Grouping", List.of(
                    new Param("Expr", "expression"))));
            add(new Ast("Literal", List.of(
                    new Param("Object", "value"))));
            add(new Ast("Unary", List.of(
                    new Param("Token", "operator"),
                    new Param("Expr", "right"))));
        }
    };

    private record Param(String type, String name) {
        @Override
        public String toString() {
            return type + " " + name;
        }
    }

    private record Ast(String name, List<Param> params) {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        File file = new File(Path.of(outputDir, baseName + ".java").toString());
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            generateFile(writer, objects);
        }
    }

    public static void generateFile(PrintWriter writer, List<Ast> objects) {
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.print("abstract class " + baseName + " {");
        objects.forEach(ast -> generateAst(writer, ast));
        writer.println("}");
    }

    public static void generateAst(PrintWriter writer, Ast ast) {
        writer.println();
        writer.println(pad1 + "static class " + ast.name + " extends " + baseName + " {");
        ast.params.forEach(p -> writer.println(pad2 + "final " + p + ";"));
        writer.println();
        String params = ast.params.stream().map(Param::toString).collect(Collectors.joining(", "));
        writer.println(pad2 + ast.name + "(" + params + ") {");
        ast.params.forEach(p -> writer.println(pad3 + "this." + p.name + " = " + p.name + ";"));
        writer.println(pad2 + "}");
        writer.println(pad1 + "}");
    }
}
