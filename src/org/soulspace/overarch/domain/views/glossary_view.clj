(ns org.soulspace.overarch.domain.views.glossary-view
  (:require [org.soulspace.overarch.domain.view :as view]
            [org.soulspace.overarch.domain.model :as model]))

(defmethod view/render-element? :glossary-view
  [view e]
  (contains? model/glossary-types (:el e)))

(defmethod view/render-content? :glossary-view
  [view e]
  (contains? model/glossary-types (:el e)))

