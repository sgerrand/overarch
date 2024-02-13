(ns org.soulspace.overarch.application.model-repository
  (:require [org.soulspace.overarch.domain.element :as el]
            [org.soulspace.overarch.domain.model :as model]
            [org.soulspace.overarch.domain.spec :as spec]
            [org.soulspace.overarch.domain.view :as view]))

(defn repo-type
  "Returns the repository type."
  ([rtype]
   rtype)
  ([rtype _]
   rtype))

(defmulti read-models
  "Reads the models with the repository of type `rtype` from all locations of the given `path`."
  repo-type)


;;
;; Application state
;;
; Application state is not needed for the overarch CLI, but maybe helpful for other clients
(def state (atom {}))

(defn update-state!
  "Updates the state with the registered data read from `path`."
  [path]
  (->> path
       ; TODO don't hardcode repo type
       (read-models :file)
       (spec/check)
       (model/build-registry)
       (reset! state)))

(defn elements
  "Returns the set of elements."
  []
  (:elements @state))

(defn update-acc
  "Update the accumulator `acc` of the hierarchical model with the element `e`."
  [acc p e]
  (cond
    ; TODO convert (hierarchical) nodes
    (el/model-node? e) (assoc acc :nodes (conj (:nodes acc) (dissoc e :ct)))
    (el/relation? e) (assoc acc :relations (conj (:relations acc) e))
    (view/view? e)  (assoc acc :views (conj (:views acc) e))
    :else acc))

(defn relational-model-fn
  "Step function for the conversion of the hierachical input model into a relational model of nodes, relations and views."
  ([] [{:nodes #{}
       :relations #{}
       :views #{}} '()])
  ([[res ctx]]
   (if-not (empty? ctx)
     [res (pop ctx)]
     res))
  ([[res ctx] e]
   (let [p (peek ctx)]
     [(update-acc res p e) (conj ctx e)])))

;  ([[res ctx] e]
;   (println "Element" (:id e) (:name e))
;   (println "Accu Pre" acc)
;   (let [new-acc (update-relational-acc acc e)]
;     (println "Accu Post" new-acc)
;     new-acc)))

(defn build-relational-model
  "Builds a relational working model from the hierarchical inpur model."
  []
  (model/traverse relational-model-fn (elements)))


(comment
  (update-state! "models")
  
  (= (:parents @state) (model/traverse model/id->parent (:elements @state)))
  (= (:registry @state) (model/traverse el/identifiable? model/id->element (:elements @state)))
  (= (:parents @state) (model/traverse el/model-node? model/id->parent (:elements @state)))
  (= (:referred @state) (model/traverse el/relation? model/referred-id->rel (:elements @state)))
  (= (:referrer @state) (model/traverse el/relation? model/referrer-id->rel (:elements @state)))
  (build-relational-model)

  ;
  :rcf)
