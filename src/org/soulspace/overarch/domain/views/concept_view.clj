(ns org.soulspace.overarch.domain.views.concept-view
  (:require [org.soulspace.overarch.domain.view :as view]
            [org.soulspace.overarch.domain.model :as model]))

(defmethod view/render-model-node? :concept-view
  [view e]
  (contains? model/concept-types (:el e)))

(defmethod view/include-content? :concept-view
  [view e]
  (contains? model/concept-types (:el e)))

(defmethod view/render-relation-node? :concept-view
  [view e]
  (view/render-model-node? view e))

(defmethod view/element-to-render :concept-view
  [view e]
  e)