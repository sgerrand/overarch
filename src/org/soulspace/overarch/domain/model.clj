;;;;
;;;; Functions for the definition and handling of the overarch model
;;;;
(ns org.soulspace.overarch.domain.model
  "Functions for the definition and handling of the overarch model.

   The loaded overarch working model is a map with these keys:
   
   :input-elements         -> the given data
   :nodes                  -> the set of nodes (incl. child nodes)
   :relations              -> the set of relations (incl. contains relations)
   :views                  -> the set of views
   :themes                 -> the set of themes
   :id->element            -> a map from id to element (nodes, relations and views)
   :id->parent             -> a map from id to parent element
   :referrer-id->relations -> a map from id to set of relations where the id is the referrer (:from)
   :referred-id->relations -> a map from id to set of relations where the id is referred (:to)
"
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [org.soulspace.overarch.util.functions :as fns]
            [org.soulspace.overarch.domain.element :as el]))

;;;
;;; Accessor functions
;;;

(defn model-element
  "Returns the model element with the given `id`."
  ([model id]
   ((:id->element model) id)))

(defn resolve-ref
  "Resolves the model element for the ref `r`."
  [model r]
  (if-let [e (model-element model (:ref r))]
    (merge e (dissoc r :ref))
    {:unresolved-ref (:ref r)}))

(defn resolve-id
  "Resolves the model element for the `id`"
  [model id]
  (if-let [e (model-element model id)]
    e
    {:unresolved-ref id}))

(defn resolve-element
  "Resolves the model element for the ref `e`."
  ([model e]
   (cond
     (keyword? e) (resolve-id model e)
     (el/reference? e) (resolve-ref model e)
     :else e)))

;;
;; Model transducer functions
;;
(defn unresolved-refs-xf 
  "Returns a transducer to extract unresolved refs"
  [model]
  (comp (filter el/reference?)
        (map (partial resolve-ref model))
        (filter el/unresolved-ref?)))

;;
;; Accessors
;;
(defn input-elements
  "Returns the collection of elements."
  [model]
  (:input-elements model))

(defn nodes
  "Returns the collection of model nodes."
  [model]
  (:nodes model))

(defn relations
  "Returns the collection of model relations."
  [model]
  (:relations model))

(defn model-elements
  "Returns the collection of model elements."
  ([model]
   (concat (nodes model) (relations model))))

(defn views
  "Returns the set of views from the `model`."
  [model]
  (:views model))

(defn themes
  "Returns the set of themes from the `model`."
  [model]
  (:themes model))

(defn parent
  "Returns the parent of the model node `e`."
  [model e]
  ((:id->parent model) (:id e)))

(defn ancestor-nodes
  "Returns the ancestor nodes of the model node `e` in the `model`."
  [model e]
  (loop [acc #{} p (parent model e)]
    (if (seq p)
      (recur (conj acc p) (parent model p))
      acc)))

(defn ancestor-node?
  "Returns true, if `c` is an ancestor of node `e` in the `model`."
  [model e c]
  (loop [p (parent model e)]
    (if (seq p)
      (if (= (:id p) (:id c))
        true
        (recur (parent model p)))
      false)))

(defn all-elements
  "Returns a set of all elements."
  ([model]
   (->> (:id->element model)
        (vals)
        (map (partial resolve-element model))
        (into #{}))))

(defn from-name
  "Returns the name of the from reference of the relation."
  [model rel]
  (->> rel
       (:from)
       (resolve-id model)
       (:name)))

(defn to-name
  "Returns the name of the from reference of the relation."
  [model rel]
  (->> rel
       (:to)
       (resolve-id model)
       (:name)))

(defn related
  "Returns the related elements for the given collection of relations"
  ([model coll]
   (->> coll
        (mapcat (fn [e] [(:from e) (:to e)]))
        (map (partial resolve-element model))
        (into #{}))))

(defn relations-of-nodes
  "Returns the relations of the model `m` connecting nodes from the given collection of model nodes."
  ([model coll]
   (let [els (into #{} (map :id coll))
         rels (filter el/model-relation? (model-elements model))
         filtered (->> rels
                       (filter (fn [r] (and (contains? els (:from r))
                                            (contains? els (:to r))))))
         _ (fns/data-tapper {:els els :rels rels :filtered filtered})]
     filtered)))

(defn related-nodes
  "Returns the set of nodes of the model `m` that are part of at least one relation in the `coll`."
  [model coll]
  (->> coll
       (filter el/model-relation?)
       (map (fn [rel] #{(resolve-element model (:from rel))
                        (resolve-element model (:to rel))}))
       (reduce set/union #{})))

(defn aggregable-relation?
  "Returns true, if the relations `r1` and `r2` are aggregable."
  ([model r1 r2]
   (and (= (:tech r1) (:tech r2))
        ; (= (:name r1) (:name r2))
        ; (= (:desc r1) (:desc r2))
        (or (= (:from r1) (:from r2))
            (= (parent model (:from r1))
               (parent model (:from r2))))
        (or (= (:to r1) (:to r2))
            (= (parent model (:to r1))
               (parent model (:to r2)))))))

;;;
;;; Build model
;;;
(defn parent-of-relation
  "Returns a parent-of relation for parent `p` and element `e`."
  [p-id e-id]
  {:el :contains
   :id (el/generate-relation-id :contains p-id e-id)
   :from p-id
   :to e-id
   :name "contains"
   :synthetic true})

(defn update-acc
  "Update the accumulator `acc` of the model with the element `e`
   in the context of the parent `p` (if given)."
  [acc p e]
  (cond
    ;; nodes
    ;; TODO add syntetic ids for nodes without ids (e.g. fields, methods)
    (el/model-node? e)
    (if (el/child? e p)
      ; a child node, add a parent-of relationship, too
      (let [r (parent-of-relation (:id p) (:id e))]
        (assoc acc
               :nodes
               (conj (:nodes acc) e)

               :relations
               (conj (:relations acc) r)

               :id->element
               (assoc (:id->element acc)
                      (:id e) e
                      (:id r) r)

               ; currently only one parent is supported here
               ; all parents are reachable via :contains relations
               :id->parent
               (assoc (:id->parent acc) (:id e) p)

               :referrer-id->relations
               (assoc (:referrer-id->relations acc)
                      (:from r)
                      (conj (get-in acc [:referrer-id->relations (:from r)] #{}) r))

               :referred-id->relations
               (assoc (:referred-id->relations acc)
                      (:to r)
                      (conj (get-in acc [:referred-id->relations (:to r)] #{}) r))))

      ; not a child node, just add the node
      (assoc acc
             :nodes
             (conj (:nodes acc) e)

             :id->element
             (assoc (:id->element acc) (:id e) e)))

    ;; relations
    (el/model-relation? e)
    (assoc acc
           :relations
           (conj (:relations acc) e)

           :id->element
           (assoc (:id->element acc) (:id e) e)

           :referrer-id->relations
           (assoc (:referrer-id->relations acc)
                  (:from e)
                  (conj (get-in acc [:referrer-id->relations (:from e)] #{}) e))

           :referred-id->relations
           (assoc (:referred-id->relations acc)
                  (:to e)
                  (conj (get-in acc [:referred-id->relations (:to e)] #{}) e)))

    ;; views
    (el/view? e)
    (assoc acc
           :views
           (conj (:views acc) e)

           :id->element
           (assoc (:id->element acc) (:id e) e))

    ;; references
    (el/reference? e)
    (if (el/model-node? p)
      ; reference is a child of a node, add a parent-of relationship
      (let [r (parent-of-relation (:id p) (:ref e))]
        (assoc acc
               :relations
               (conj (:relations acc) r)

               :id->element
               (assoc (:id->element acc) (:id r) r)

               ; currently only one parent is supported here
               :id->parent
               (assoc (:id->parent acc) (:id e) p)

               :referrer-id->relations
               (assoc (:referrer-id->relations acc)
                      (:from r)
                      (conj (get-in acc [:referrer-id->relations (:from r)] #{}) r))

               :referred-id->relations
               (assoc (:referred-id->relations acc)
                      (:to r)
                      (conj (get-in acc [:referred-id->relations (:to r)] #{}) r))))
      ; reference is a child of a view, leave as is
      acc)

    ;; themes
    (el/theme? e)
    (assoc acc
       :themes
       (conj (:themes acc) e)

       :id->element
       (assoc (:id->element acc) (:id e) e))

    ; unhandled element
    :else (do (println "Unhandled:" e) acc)))

(defn ->relational-model
  "Step function for the conversion of the hierachical input model into a relational model of nodes, relations and views."
  ([]
   ; initial compound accumulator with empty model and context
   [{:nodes #{}
     :relations #{}
     :views #{}
     :themes #{}
     :id->element {}
     :id->parent {}
     :referred-id->relations {}
     :referrer-id->relations {}}
    '()])
  ([[res ctx]]
   ; return result from accumulator
   (if-not (empty? ctx)
     ; not done yet because context stack is not empty
     ; pop element from stack and return accumulator with
     ; current resulting model and popped context
     [res (pop ctx)]
     res))
  ([[res ctx] e]
   ; update accumulator in step by calling update function
   ; with result, parent from context stack (if any) and
   ; the current element. Also push current element to context stack.
   (let [p (peek ctx)]
     [(update-acc res p e) (conj ctx e)])))

(defn build-model
  "Builds the working model from the input `coll` of elements."
  [coll]
  (let [relational (el/traverse ->relational-model coll)]
    (assoc relational :input-elements coll)))


;;;
;;; filtering element colletions by criteria
;;;
;; TODO add docstrings
(defn child?
  [model v e]
  (= v (boolean ((:id->parent model) (:id e)))))

(defn refers-check?
  [model v e]
  (= v (boolean ((:referrer-id->relations model) (:id e)))))

(defn referred-check?
  [model v e]
  (= v (boolean ((:referred-id->relations model) (:id e)))))

(defn refers-to?
  [model v e]
  (contains? (into #{}
                   (map :to ((:referrer-id->relations model) (:id e))))
             v))

(defn referred-by?
  [model v e]
  (contains? (into #{}
                   (map :from ((:referred-id->relations model) (:id e))))
             v))

(defn descendant-of?
  [model v e]
  (contains? (el/descendant-nodes (resolve-id model v)) e))

(defn ancestor-of?
  [model v e]
  (contains? (ancestor-nodes model (resolve-id model v)) e))

(defn criterium-predicate
  "Returns a predicate for the given `criterium`."
  [model [k v]]
; TODO add criteria e.g. :parent :parent? :relation-of
  (cond
    ;;
    ;; element related
    ;;
    (= :key? k)                  (partial el/key-check? v)
    (= :key k)                   (partial el/key? v)
    (= :el k)                    (partial el/el? v)
    (= :els k)                   (partial el/els? v)
    (= :namespace k)             (partial el/namespace? v)
    (= :namespaces k)            (partial el/namespaces? v)
    (= :namespace-prefix k)      (partial el/namespace-prefix? v)
    (= :from-namespace k)        (partial el/from-namespace? v)
    (= :from-namespaces k)       (partial el/from-namespaces? v)
    (= :from-namespace-prefix k) (partial el/from-namespace-prefix? v)
    (= :to-namespace k)          (partial el/to-namespace? v)
    (= :to-namespaces k)         (partial el/to-namespaces? v)
    (= :to-namespace-prefix k)   (partial el/to-namespace-prefix? v)
    (= :id? k)                   (partial el/id-check? v)
    (= :id k)                    (partial el/id? v)
    (= :from k)                  (partial el/from? v)
    (= :to k)                    (partial el/to? v)
    (= :subtype? k)              (partial el/subtype-check? v)
    (= :subtype k)               (partial el/subtype? v)
    (= :subtypes k)              (partial el/subtypes? v)
    (= :external? k)             (partial el/external-check? v)
    (= :name? k)                 (partial el/name-check? v)
    (= :name k)                  (partial el/name? v)
    (= :desc? k)                 (partial el/desc-check? v)
    (= :tech? k)                 (partial el/tech-check? v)
    (= :tech k)                  (partial el/tech? v)
    (= :techs k)                 (partial el/techs? v)
    (= :all-techs k)             (partial el/all-techs? v)
    (= :tags? k)                 (partial el/tags-check? v)
    (= :tag k)                   (partial el/tag? v)
    (= :tags k)                  (partial el/tags? v)
    (= :all-tags k)              (partial el/all-tags? v)
    (= :children? k)             (partial el/children-check? v)

    ;; model related
    (= :child? k)                (partial child? model v)
    (= :refers? k)               (partial refers-check? model v)
    (= :referred? k)             (partial referred-check? model v)
    (= :refers-to k)             (partial refers-to? model v)
    (= :referred-by k)           (partial referred-by? model v)
    (= :ancestor-of k)           (partial ancestor-of? model v) ; TODO docs
    (= :descendant-of k)         (partial descendant-of? model v) ; TODO docs

    :else
    (println "unknown criterium" (name k))))

(defn criteria-predicate
  "Returns a filter predicate for the given `criteria`.
   The resulting predicate is a logical conjunction (and) of the predicates for
   each criterium."
  [model criteria]
  (loop [remaining criteria
         predicates []]
    (if (seq remaining)
      (recur (rest remaining)
             (conj predicates (criterium-predicate model (first remaining))))
      ; add a predicate which returns true to return at least one predicate
      (apply every-pred (conj (remove nil? predicates) (fn [_] true))))))

(defn criteria-predicates
  "Returns a filter predicate for the given `criteria`.
   If a vector of criteria maps is given, the resulting predicate is a logical
   disjunction (or) of the predicates."
  [model criteria]
  (if (vector? criteria)
    ; vector of criteria
    (loop [remaining criteria
           predicates []]
      (if (seq remaining)
        (recur (rest remaining)
               (conj predicates (criteria-predicate model (first remaining))))
        (apply some-fn predicates)))
    ; simple criteria map
    (criteria-predicate model criteria)))

(defn filter-xf
  "Returns a filter transducer for the given `criteria`."
  [model criteria]
  (let [filter-predicates (criteria-predicates model criteria)]
    ; compose the filtering functions and create a filter transducer
    (filter filter-predicates)))

