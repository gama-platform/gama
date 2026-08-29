package gama.ui.devtools.dependencies;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import gama.ui.devtools.dependencies.DependencyGraphBuilder;

/**
 * Convertisseur DOT vers HTML interactif (Version Améliorée).
 * Offre un design moderne, une recherche fluide et une navigation bidirectionnelle.
 */
public class GraphToHtml {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java GraphToHtml <input.dot> <output.html>");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];

        try {
            DependencyGraphBuilder builder = new DependencyGraphBuilder();

            // builder.resolveLibrariesInDirectory("/path/to/libraries/directory");
            builder.fromDot(args[0]);

            generateHtml(builder, outputPath);

            System.out.println("generation was successful");
        } catch (Exception e) {
            System.err.println("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generateHtml(DependencyGraphBuilder builder, String outputPath) {
        Set<String> libraries = builder.getDependenciesGraph().vertexSet();

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html lang=\"fr\">\n<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Dependency Graph Explorator</title>\n");
        html.append("    <style>\n");
        html.append("        :root {\n");
        html.append("            --primary: #4f46e5;\n");
        html.append("            --primary-dark: #4338ca;\n");
        html.append("            --bg-sidebar: #1e293b;\n");
        html.append("            --bg-main: #f8fafc;\n");
        html.append("            --text-main: #0f172a;\n");
        html.append("            --text-muted: #64748b;\n");
        html.append("            --border: #e2e8f0;\n");
        html.append("            --card-bg: #ffffff;\n");
        html.append("        }\n");
        html.append("        body { font-family: 'Inter', system-ui, -apple-system, sans-serif; display: flex; height: 100vh; margin: 0; background: var(--bg-main); color: var(--text-main); overflow: hidden; }\n");
        
        // Sidebar
        html.append("        #sidebar { width: 300px; background: var(--bg-sidebar); color: white; display: flex; flex-direction: column; flex-shrink: 0; box-shadow: 4px 0 15px rgba(0,0,0,0.1); }\n");
        html.append("        .sidebar-header { padding: 24px; border-bottom: 1px solid rgba(255,255,255,0.1); }\n");
        html.append("        .sidebar-header h2 { margin: 0; font-size: 1.25rem; font-weight: 700; letter-spacing: -0.025em; }\n");
        html.append("        .search-container { padding: 16px; }\n");
        html.append("        .search-box { width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.1); background: rgba(255,255,255,0.05); color: white; outline: none; transition: all 0.2s; box-sizing: border-box; }\n");
        html.append("        .search-box:focus { border-color: var(--primary); background: rgba(255,255,255,0.1); }\n");
        html.append("        #nav-list { flex-grow: 1; overflow-y: auto; padding: 8px; }\n");
        html.append("        .nav-item { padding: 10px 16px; cursor: pointer; border-radius: 6px; margin-bottom: 2px; transition: all 0.15s; font-size: 0.9rem; color: #cbd5e1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }\n");
        html.append("        .nav-item:hover { background: rgba(255,255,255,0.05); color: white; }\n");
        html.append("        .nav-item.active { background: var(--primary); color: white; font-weight: 600; }\n");
        
        // Main Content
        html.append("        #content { flex-grow: 1; padding: 40px; overflow-y: auto; position: relative; }\n");
        html.append("        .vertex-view { display: none; max-width: 800px; margin: 0 auto; animation: fadeIn 0.3s ease-out; }\n");
        html.append("        .vertex-view.active { display: block; }\n");
        html.append("        @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }\n");
        
        html.append("        .header-section { margin-bottom: 32px; }\n");
        html.append("        .node-badge { display: inline-block; padding: 4px 12px; background: #e0e7ff; color: var(--primary); border-radius: 9999px; font-size: 0.75rem; font-weight: 600; margin-bottom: 8px; }\n");
        html.append("        h1 { margin: 0; font-size: 2.25rem; font-weight: 800; color: #1e293b; word-break: break-all; }\n");
        
        html.append("        .grid-container { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }\n");
        html.append("        .card { background: var(--card-bg); border: 1px solid var(--border); border-radius: 12px; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }\n");
        html.append("        .card h3 { margin-top: 0; margin-bottom: 16px; font-size: 1.1rem; color: #475569; display: flex; align-items: center; gap: 8px; }\n");
        
        html.append("        .link-list { list-style: none; padding: 0; margin: 0; }\n");
        html.append("        .link-item { margin-bottom: 8px; }\n");
        html.append("        .node-link { display: flex; align-items: center; text-decoration: none; color: var(--text-main); padding: 12px 16px; background: #f1f5f9; border-radius: 8px; font-weight: 500; transition: all 0.2s; border: 1px solid transparent; }\n");
        html.append("        .node-link:hover { background: #fff; border-color: var(--primary); color: var(--primary); transform: translateX(4px); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }\n");
        // html.append("        .node-link::after { content: '→'; margin-left: auto; opacity: 0.5; }\n");
        
        html.append("        .node-simple { display: flex; align-items: center; text-decoration: none; color: var(--text-main); padding: 12px 16px; background: #f1f5f9; border-radius: 8px; font-weight: 500; transition: all 0.2s; border: 1px solid transparent; }\n");
        html.append("        .node-simple:hover { cursor: default !important }\n");

        html.append("        .direct-relation::after { content: 'direct →'; margin-left: auto; font-size: x-small; opacity: 0.5; }");

        html.append("        .recursive-relation::after { content: 'recursive →'; margin-left: auto; font-size: x-small; opacity: 0.5; }");

        html.append("        .empty-state { color: var(--text-muted); font-style: italic; text-align: center; padding: 20px; border: 2px dashed var(--border); border-radius: 8px; }\n");
        html.append("        .stats-badge { font-size: 0.8rem; padding: 2px 8px; background: #f1f5f9; border-radius: 4px; margin-left: auto; }\n");
        html.append("    </style>\n");
        html.append("</head>\n<body>\n");

        // Sidebar
        html.append("    <div id=\"sidebar\">\n");
        html.append("        <div class=\"sidebar-header\">\n");
        html.append("            <h2>Graph Explorer</h2>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"search-container\">\n");
        html.append("            <input type=\"text\" class=\"search-box\" placeholder=\"Filter library...\" onkeyup=\"filterNodes(this.value)\">\n");
        html.append("        </div>\n");
        html.append("        <div id=\"nav-list\">\n");
        for (String library : libraries) {
            html.append("            <div class=\"nav-item\" id=\"nav-").append(library).append("\" onclick=\"showVertex('").append(library).append("')\">")
                .append(library).append("</div>\n");
        }
        html.append("        </div>\n    </div>\n");

        // Main Content
        html.append("    <div id=\"content\">\n");
        for (String library : libraries) {
            Set<String> directDependencies = builder.getDirectDependencies(library);
            Set<String> directImporters = builder.getDirectImporters(library);

            Set<String> dependencies = builder.getDependencies(library);
            dependencies.removeAll(directDependencies);
            Set<String> importers = builder.getImporters(library);
            importers.removeAll(directImporters);

            Set<String> directConsumers = builder.getLibraryDirectConsumers(library);
            Set<String> directProviders = builder.getLibraryDirectProviders(library);

            Set<String> consumers = builder.getLibraryConsumers(library);
            consumers.removeAll(directConsumers);
            Set<String> providers = builder.getLibraryProviders(library);
            providers.removeAll(directProviders);

            Set<String> exposedServices = builder.getLibraryServices(library);

            html.append("        <div class=\"vertex-view\" id=\"view-").append(library).append("\">\n");
            html.append("            <div class=\"header-section\">\n");
            html.append("                <span class=\"node-badge\">Library</span>\n");
            html.append("                <h1>").append(library).append("</h1>\n");
            // html.append("                <h2>").append(builder.getLibraryPath(library).toString()).append("</h2>\n");
            html.append("            </div>\n");
            
            html.append("            <div class=\"grid-container\">\n");
            
            // importers
            html.append("                <div class=\"card\">\n");
            html.append("                    <h3>Importers <span class=\"stats-badge\">").append(directImporters.size() + importers.size()).append("</span></h3>\n");
            if (directImporters.isEmpty() && importers.isEmpty()) {
                html.append("                    <div class=\"empty-state\">No Importers</div>\n");
            } else {
                html.append("                    <div class=\"link-list\">\n");
                for (String p : directImporters) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-link direct-relation\" onclick=\"showVertex('")
                        .append(p).append("')\">").append(p).append("</a></div>\n");
                }
                for (String p : importers) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-link recursive-relation\" onclick=\"showVertex('")
                        .append(p).append("')\">").append(p).append("</a></div>\n");
                }
                html.append("                    </div>\n");
            }
            html.append("                </div>\n");

            // dependencies
            html.append("                <div class=\"card\">\n");
            html.append("                    <h3>Dependencies <span class=\"stats-badge\">").append(directDependencies.size() + dependencies.size()).append("</span></h3>\n");
            if (directDependencies.isEmpty() && dependencies.isEmpty()) {
                html.append("                    <div class=\"empty-state\">No dependencies</div>\n");
            } else {
                html.append("                    <div class=\"link-list\">\n");
                for (String s : directDependencies) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-link direct-relation\" onclick=\"showVertex('")
                        .append(s).append("')\">").append(s).append("</a></div>\n");
                }
                for (String s : dependencies) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-link recursive-relation\" onclick=\"showVertex('")
                        .append(s).append("')\">").append(s).append("</a></div>\n");
                }
                html.append("                    </div>\n");
            }
            html.append("                </div>\n");
            
            // consumers
            html.append("                <div class=\"card\">\n");
            html.append("                    <h3>Potential service consumers <span class=\"stats-badge\">").append(directConsumers.size() + consumers.size()).append("</span></h3>\n");
            if (consumers.isEmpty() && directConsumers.isEmpty()) {
                html.append("                    <div class=\"empty-state\">No consumers</div>\n");
            } else {
                html.append("                    <div class=\"link-list\">\n");
                for (String p : directConsumers) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-link direct-relation\" onclick=\"showVertex('")
                        .append(p).append("')\">").append(p).append("</a></div>\n");
                }
                for (String p : consumers) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-link recursive-relation\" onclick=\"showVertex('")
                        .append(p).append("')\">").append(p).append("</a></div>\n");
                }
                html.append("                    </div>\n");
            }
            html.append("                </div>\n");

            // providers
            html.append("                <div class=\"card\">\n");
            html.append("                    <h3>Potential service providers <span class=\"stats-badge\">").append(directProviders.size() + providers.size()).append("</span></h3>\n");
            if (providers.isEmpty() && directProviders.isEmpty()) {
                html.append("                    <div class=\"empty-state\">No providers</div>\n");
            } else {
                html.append("                    <div class=\"link-list\">\n");
                for (String p : directProviders) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-link direct-relation\" onclick=\"showVertex('")
                        .append(p).append("')\">").append(p).append("</a></div>\n");
                }
                for (String p : providers) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-link recursive-relation\" onclick=\"showVertex('")
                        .append(p).append("')\">").append(p).append("</a></div>\n");
                }
                html.append("                    </div>\n");
            }
            html.append("                </div>\n");

            // exposed services
            html.append("                <div class=\"card\">\n");
            html.append("                    <h3>Exposed Services <span class=\"stats-badge\">").append(exposedServices.size()).append("</span></h3>\n");
            if (exposedServices.isEmpty()) {
                html.append("                    <div class=\"empty-state\">No exposed services</div>\n");
            } else {
                html.append("                    <div class=\"link-list\">\n");
                for (String s : exposedServices) {
                    html.append("                        <div class=\"link-item\"><a href=\"#\" class=\"node-simple\">").append(s).append("</a></div>\n");
                }
                html.append("                    </div>\n");
            }
            html.append("                </div>\n");
            
            html.append("            </div>\n"); // End grid
            html.append("        </div>\n"); // End vertex-view
        }
        html.append("    </div>\n");

        // JS
        html.append("    <script>\n");
        html.append("        function showVertex(nodeId) {\n");
        html.append("            document.querySelectorAll('.vertex-view, .nav-item').forEach(el => el.classList.remove('active'));\n");
        html.append("            const view = document.getElementById('view-' + nodeId);\n");
        html.append("            const nav = document.getElementById('nav-' + nodeId);\n");
        html.append("            if (view) view.classList.add('active');\n");
        html.append("            if (nav) {\n");
        html.append("                nav.classList.add('active');\n");
        html.append("                nav.scrollIntoView({ behavior: 'smooth', block: 'nearest' });\n");
        html.append("            }\n");
        html.append("            window.scrollTo({ top: 0, behavior: 'smooth' });\n");
        html.append("        }\n");
        html.append("        function filterNodes(query) {\n");
        html.append("            const items = document.querySelectorAll('.nav-item');\n");
        html.append("            query = query.toLowerCase();\n");
        html.append("            items.forEach(item => {\n");
        html.append("                item.style.display = item.textContent.toLowerCase().includes(query) ? 'block' : 'none';\n");
        html.append("            });\n");
        html.append("        }\n");
        html.append("        const first = \"").append(libraries.isEmpty() ? "" : libraries.iterator().next()).append("\";\n");
        html.append("        if (first) showVertex(first);\n");
        html.append("    </script>\n");
        html.append("</body>\n</html>\n");

        try {
            Files.writeString(Paths.get(outputPath), html.toString());
        } catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
