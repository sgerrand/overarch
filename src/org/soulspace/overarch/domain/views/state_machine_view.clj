(ns org.soulspace.overarch.domain.views.state-machine-view
  (:require [org.soulspace.overarch.domain.element :as el]
            [org.soulspace.overarch.domain.view :as view]))

(defmethod view/render-model-element? :state-machine-view
  [view e]
  (contains? el/state-machine-view-element-types (:el e)))

(defmethod view/include-content? :state-machine-view
  [view e]
  (contains? el/state-machine-view-element-types (:el e)))

(defmethod view/render-relation-node? :state-machine-view
  [view e]
  (view/render-model-element? view e))

(defmethod view/element-to-render :state-machine-view
  [view e]
  e)
