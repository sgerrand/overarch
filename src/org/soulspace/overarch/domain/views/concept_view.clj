(ns org.soulspace.overarch.domain.views.concept-view
  (:require [org.soulspace.overarch.domain.element :as e]
            [org.soulspace.overarch.domain.view :as view]))

(defmethod view/render-model-node? :concept-view
  [view e]
  (contains? e/concept-view-types (:el e)))

(defmethod view/include-content? :concept-view
  [view e]
  (contains? e/concept-view-types (:el e)))

(defmethod view/render-relation-node? :concept-view
  [view e]
  (view/render-model-node? view e))

(defmethod view/element-to-render :concept-view
  [view e]
  e)
