;;;;
;;;; GraphViz rendering and export
;;;;
(ns org.soulspace.overarch.exports.graphviz
  "Functions to export views to GraphViz."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [org.soulspace.clj.string :as sstr]
            [org.soulspace.clj.java.file :as file]
            [org.soulspace.overarch.core :as core]
            [org.soulspace.overarch.view :as view]
            [org.soulspace.overarch.export :as exp]
            [org.soulspace.overarch.exports.graphviz :as graphviz]))

;;;
;;; Rendering
;;;
(defn alias-name
  "Returns a valid PlantUML alias for the namespaced keyword `kw`."
  [kw]
  (symbol (str (str/replace (sstr/hyphen-to-camel-case (namespace kw)) \. \_) "_"
               (sstr/hyphen-to-camel-case (name kw)))))
(defn short-name
  "Returns a valid PlantUML alias for the name part of the keyword `kw`."
  [kw]
  (sstr/hyphen-to-camel-case (name kw)))

(def element-hierarchy
  "Hierarchy for elements to render."
  (-> (make-hierarchy)
      (derive :enterprise-boundary :architecture-model-element)
      (derive :context-boundary    :architecture-model-element)
      (derive :system              :architecture-model-element)
      (derive :container           :architecture-model-element)))

(defmulti render-element
  "Renders an element `e` in the `view` with markdown according to the given `options`."
  (fn [e _ _] (:el e))
  :hierarchy #'element-hierarchy)

(defmethod render-element :concept
  [e indent view]
  [(str (alias-name (:id e)) "[label=\"" (:name e) "\"];")])

(defmethod render-element :architecture-model-element
  [e indent view]
  [(str (alias-name (:id e)) "[label=\"" (:name e) "\"];")])

(defmethod render-element :person
  [e indent view]
  [(str (alias-name (:id e)) "[label=\"" (:name e) "\"];")])

(defmethod render-element :rel
  [e indent view]
  [(str (alias-name (:from e)) " -> " (alias-name (:to e))
        " [label=\"" (:name e) "\"];")])

(defn render-layout
  "Renders the layout options for the `view`."
  [view]
  (let [spec (:spec view)
        graphviz (:graphviz spec)]
    (flatten [(when (= :left-right (:layout spec))
                "rankdir=\"LR\"")
              (when (:engine graphviz)
                (str "layout=\"" (name (:engine graphviz)) "\""))])))

(defn render-view
  "Renders the `view` with graphviz according to the given `options`."
  [options view]
  (let [children (sort-by :name (view/elements-in-view view))]
    (flatten [(str "digraph \"" (:title view) "\" {")
              "labelloc= \"t\""
              (str "label=\"" (:title view) "\"")
              (render-layout view)
              (map #(render-element % 0 view) children)
              "}"])))

;;;
;;; Export
;;;
(def graphviz-views
  "Contains the views rendered with graphviz."
  #{:concept-view})

(defn graphviz-view?
  "Returns true, if the view is to be rendered with graphviz."
  [view]
  (contains? graphviz-views (:el view)))

(defmethod exp/export-file :graphviz
  [options view]
  (let [dir-name (str (:export-dir options) "/graphviz/"
                      (namespace (:id view)))]
    (file/create-dir (io/as-file dir-name))
    (io/as-file (str dir-name "/"
                     (name (:id view)) ".dot"))))

(defmethod exp/export-view :graphviz
  [options view]
  (with-open [wrt (io/writer (exp/export-file options view))]
    (binding [*out* wrt]
      (println (str/join "\n" (render-view options view))))))

(defmethod exp/export :graphviz
  [options]
  (doseq [view (core/get-views)]
    (when (graphviz-view? view)
      (exp/export-view options view))))
