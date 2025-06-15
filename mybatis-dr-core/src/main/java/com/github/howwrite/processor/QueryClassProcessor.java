package com.github.howwrite.processor;

import com.github.howwrite.annotation.DrColumn;
import com.github.howwrite.annotation.DrTable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 查询类生成器，用于生成查询类
 *
 * @author mybatis-dr
 */
@SupportedAnnotationTypes("com.github.howwrite.annotation.DrTable")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class QueryClassProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(DrTable.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            DrTable drTable = element.getAnnotation(DrTable.class);

            TypeElement classElement = (TypeElement) element;
            String packageName = processingEnv.getElementUtils().getPackageOf(classElement).toString();
            String className = classElement.getSimpleName().toString();
            String queryClassName = className + "Query";

            // 收集可查询字段
            List<FieldInfo> queryableFields = new ArrayList<>();

            for (Element field : classElement.getEnclosedElements()) {
                if (field.getKind() != ElementKind.FIELD) {
                    continue;
                }

                DrColumn drColumnAnnotation = field.getAnnotation(DrColumn.class);
                if (drColumnAnnotation != null) {
                    String fieldName = field.getSimpleName().toString();
                    String columnName = drColumnAnnotation.value();

                    String fieldType = field.asType().toString();
                    boolean query = drColumnAnnotation.query();

                    queryableFields.add(new FieldInfo(fieldName, columnName, fieldType, query));
                }
            }

            // 生成查询类
            try {
                generateQueryClass(drTable, packageName, className, queryClassName, queryableFields);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Error generating query class: " + e.getMessage(), element);
            }
        }

        return true;
    }

    /**
     * 生成查询类
     *
     * @param drTable
     * @param packageName     包名
     * @param className       类名
     * @param queryClassName  查询类名
     * @param queryableFields 可查询字段
     * @throws IOException IO异常
     */
    private void generateQueryClass(DrTable drTable, String packageName, String className, String queryClassName,
                                    List<FieldInfo> queryableFields) throws IOException {
        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(packageName + "." + queryClassName);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // 包名
            out.println("package " + packageName + ";");
            out.println();

            // 导入
            out.println("import com.github.howwrite.query.QueryCondition;");
            out.println("import java.util.Collection;");
            out.println();

            // 类声明
            out.println("/**");
            out.println(" * " + className + "的查询类，由mybatis-dr自动生成");
            out.println(" */");
            out.println("public class " + queryClassName + " extends QueryCondition<" + packageName + "." + className + ", " + queryClassName + "> {");
            out.println();

            // currentEntityClass
            out.println("    /**");
            out.println("     * currentEntityClass");
            out.println("     */");
            out.println("    public Class<" + packageName + "." + className + "> currentEntityClass(){");
            out.println("        return " + packageName + "." + className + ".class;");
            out.println("    }");
            out.println();

            // 为每个字段生成查询方法
            for (FieldInfo field : queryableFields) {
                generateFieldQueryMethods(out, drTable, queryClassName, field);
            }

            // 类结束
            out.println("}");
        }
    }

    /**
     * 为字段生成查询方法
     *
     * @param out            输出流
     * @param field          字段信息
     */
    private void generateFieldQueryMethods(PrintWriter out, DrTable drTable, String queryClassName, FieldInfo field) {
        String fieldName = field.getFieldName();
        String columnName = field.getColumnName();
        String capitalizedFieldName = capitalize(fieldName);
        String fieldType = getSimpleTypeName(field.getFieldType());

        out.println("   public static SelectKey select" + capitalizedFieldName + " = new SelectKey(\"" + (field.isQueryField() ? columnName : drTable.featureColumnName()) + "\");");
        out.println();

        if (!field.isQueryField()) {
            // 非查询字段没有后面的条件
            return;
        }

        // 等于
        out.println("    /**");
        out.println("     * " + fieldName + " 等于");
        out.println("     */");
        out.println("    public " + queryClassName + " eq" + capitalizedFieldName + "(" + fieldType + " value) {");
        out.println("        return (" + queryClassName + ") eq(\"" + columnName + "\", value);");
        out.println("    }");
        out.println();

        // 不等于
        out.println("    /**");
        out.println("     * " + fieldName + " 不等于");
        out.println("     */");
        out.println("    public " + queryClassName + " ne" + capitalizedFieldName + "(" + fieldType + " value) {");
        out.println("        return (" + queryClassName + ") ne(\"" + columnName + "\", value);");
        out.println("    }");
        out.println();

        // 数值类型特有的比较方法
        if (isNumberType(field.getFieldType())) {
            // 大于
            out.println("    /**");
            out.println("     * " + fieldName + " 大于");
            out.println("     */");
            out.println("    public " + queryClassName + " gt" + capitalizedFieldName + "(" + fieldType + " value) {");
            out.println("        return (" + queryClassName + ") gt(\"" + columnName + "\", value);");
            out.println("    }");
            out.println();

            // 大于等于
            out.println("    /**");
            out.println("     * " + fieldName + " 大于等于");
            out.println("     */");
            out.println("    public " + queryClassName + " ge" + capitalizedFieldName + "(" + fieldType + " value) {");
            out.println("        return (" + queryClassName + ") ge(\"" + columnName + "\", value);");
            out.println("    }");
            out.println();

            // 小于
            out.println("    /**");
            out.println("     * " + fieldName + " 小于");
            out.println("     */");
            out.println("    public " + queryClassName + " lt" + capitalizedFieldName + "(" + fieldType + " value) {");
            out.println("        return (" + queryClassName + ") lt(\"" + columnName + "\", value);");
            out.println("    }");
            out.println();

            // 小于等于
            out.println("    /**");
            out.println("     * " + fieldName + " 小于等于");
            out.println("     */");
            out.println("    public " + queryClassName + " le" + capitalizedFieldName + "(" + fieldType + " value) {");
            out.println("        return (" + queryClassName + ") le(\"" + columnName + "\", value);");
            out.println("    }");
            out.println();
        }

        // IN
        out.println("    /**");
        out.println("     * " + fieldName + " 包含在");
        out.println("     */");
        out.println("    public " + queryClassName + " in" + capitalizedFieldName + "(Collection<" + fieldType + "> values) {");
        out.println("        return (" + queryClassName + ") in(\"" + columnName + "\", values);");
        out.println("    }");
        out.println();

        // NOT IN
        out.println("    /**");
        out.println("     * " + fieldName + " 不包含在");
        out.println("     */");
        out.println("    public " + queryClassName + " notIn" + capitalizedFieldName + "(Collection<" + fieldType + "> values) {");
        out.println("        return (" + queryClassName + ") notIn(\"" + columnName + "\", values);");
        out.println("    }");
        out.println();

        // 字符串类型特有的LIKE方法
        if (isStringType(field.getFieldType())) {
            // LIKE
            out.println("    /**");
            out.println("     * " + fieldName + " 模糊匹配");
            out.println("     */");
            out.println("    public " + queryClassName + " like" + capitalizedFieldName + "(String value) {");
            out.println("        return (" + queryClassName + ") like(\"" + columnName + "\", value);");
            out.println("    }");
            out.println();

            // LEFT LIKE
            out.println("    /**");
            out.println("     * " + fieldName + " 左模糊匹配");
            out.println("     */");
            out.println("    public " + queryClassName + " likeLeft" + capitalizedFieldName + "(String value) {");
            out.println("        return (" + queryClassName + ") likeLeft(\"" + columnName + "\", value);");
            out.println("    }");
            out.println();

            // RIGHT LIKE
            out.println("    /**");
            out.println("     * " + fieldName + " 右模糊匹配");
            out.println("     */");
            out.println("    public " + queryClassName + " likeRight" + capitalizedFieldName + "(String value) {");
            out.println("        return (" + queryClassName + ") likeRight(\"" + columnName + "\", value);");
            out.println("    }");
            out.println();
        }

        // IS NULL
        out.println("    /**");
        out.println("     * " + fieldName + " 为空");
        out.println("     */");
        out.println("    public " + queryClassName + " isNull" + capitalizedFieldName + "() {");
        out.println("        return (" + queryClassName + ") isNull(\"" + columnName + "\");");
        out.println("    }");
        out.println();

        // IS NOT NULL
        out.println("    /**");
        out.println("     * " + fieldName + " 不为空");
        out.println("     */");
        out.println("    public " + queryClassName + " isNotNull" + capitalizedFieldName + "() {");
        out.println("        return (" + queryClassName + ") isNotNull(\"" + columnName + "\");");
        out.println("    }");
        out.println();

        // desc
        out.println("    /**");
        out.println("     * " + fieldName + " 降序排序");
        out.println("     */");
        out.println("    public " + queryClassName + " desc" + capitalizedFieldName + "() {");
        out.println("        return (" + queryClassName + ") desc(\"" + columnName + "\");");
        out.println("    }");
        out.println();

        // asc
        out.println("    /**");
        out.println("     * " + fieldName + " 升序排序");
        out.println("     */");
        out.println("    public " + queryClassName + " asc" + capitalizedFieldName + "() {");
        out.println("        return (" + queryClassName + ") asc(\"" + columnName + "\");");
        out.println("    }");
        out.println();
    }

    /**
     * 获取简单类型名
     *
     * @param typeName 类型全名
     * @return 简单类型名
     */
    private String getSimpleTypeName(String typeName) {
        if (typeName.equals("java.lang.String")) {
            return "String";
        } else if (typeName.equals("java.lang.Integer")) {
            return "Integer";
        } else if (typeName.equals("java.lang.Long")) {
            return "Long";
        } else if (typeName.equals("java.lang.Boolean")) {
            return "Boolean";
        } else if (typeName.equals("java.lang.Float")) {
            return "Float";
        } else if (typeName.equals("java.lang.Double")) {
            return "Double";
        } else if (typeName.equals("java.util.Date")) {
            return "Date";
        } else if (typeName.equals("java.sql.Timestamp")) {
            return "Timestamp";
        } else if (typeName.equals("int")) {
            return "int";
        } else if (typeName.equals("long")) {
            return "long";
        } else if (typeName.equals("boolean")) {
            return "boolean";
        } else if (typeName.equals("float")) {
            return "float";
        } else if (typeName.equals("double")) {
            return "double";
        }

        return typeName;
    }

    /**
     * 判断是否为数值类型
     *
     * @param typeName 类型名
     * @return 是否为数值类型
     */
    private boolean isNumberType(String typeName) {
        return typeName.equals("java.lang.Integer") ||
                typeName.equals("java.lang.Long") ||
                typeName.equals("java.lang.Float") ||
                typeName.equals("java.lang.Double") ||
                typeName.equals("int") ||
                typeName.equals("long") ||
                typeName.equals("float") ||
                typeName.equals("double");
    }

    /**
     * 判断是否为字符串类型
     *
     * @param typeName 类型名
     * @return 是否为字符串类型
     */
    private boolean isStringType(String typeName) {
        return typeName.equals("java.lang.String");
    }

    /**
     * 首字母大写
     *
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 驼峰命名转下划线命名
     *
     * @param camelCase 驼峰命名
     * @return 下划线命名
     */
    private String camelToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camelCase.charAt(0)));

        for (int i = 1; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    /**
     * 字段信息内部类
     */
    private static class FieldInfo {
        private final String fieldName;
        private final String columnName;
        private final String fieldType;
        private final boolean queryField;

        public FieldInfo(String fieldName, String columnName, String fieldType, boolean queryField) {
            this.fieldName = fieldName;
            this.columnName = columnName;
            this.fieldType = fieldType;
            this.queryField = queryField;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getFieldType() {
            return fieldType;
        }

        public boolean isQueryField() {
            return queryField;
        }

        public String getClassName() {
            return "QueryCondition";
        }
    }
} 