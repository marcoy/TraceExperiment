(import 'java.io.PrintWriter)
(import '[org.apache.commons.io IOUtils])
(defonce type->int
  {:error 0
   :event 1
   :exit 2
   :instrument 3
   :message 4
   :rename 5
   :success 6
   :number-map 7
   :string-map 8
   :number 9
   :grid-data 10
   :retransformation-start 11
   :retransformation-class 12})


; (defonce int->cmd
;   {3 (InstrumentCommand. nil)
;    4 (MessageCommand.)})


; (defprotocol CommandIO
;   (readbytes [this ois])
;   (writebytes [this oos]))


; (defrecord InstrumentCommand [code]
;   CommandIO
;   (writebytes [this oos]
;     (let [bytecode (:code this)]
;       (do
;         (.writeByte oos (byte 3))
;         (.writeInt oos (count bytecode))
;         (.write oos bytecode)
;         (.writeInt oos (byte 0))
;         (.flush oos))))
;   (readbytes [this ois]))


; (defrecord MessageCommand []
;   CommandIO
;   (readbytes [this ois])
;   (writebytes [this oos]))

(defmulti readbytes (fn [c & _] (int c)))
(defmulti writebytes (fn [c & _] (:type c)))
(defmulti printcmd (fn [c & _] (:type c)))


;; ----------------------------------------------------------------------------
;; Instrument
;; ----------------------------------------------------------------------------
(defn instrument-command
  [bytecode args]
  {:type 3 :code bytecode :args args})

(defmethod readbytes 3
  [cmd-type ois]
  (let [len (.readInt ois)
        bytebuffer (byte-array len)
        _ (.readFully ois bytebuffer)
        args (doall (for [_ (range (.readInt ois))] (.readUTF ois)))]
    (instrument-command bytebuffer args)))

(defmethod writebytes 3
  [cmd oos]
  (when-some [bytecode (:code cmd)]
    (do
      (.writeByte oos (byte (:type cmd)))
      (.writeInt oos (count bytecode))
      (.write oos bytecode)
      (.writeInt oos (byte 0))
      (.flush oos))))


;; ----------------------------------------------------------------------------
;; Message
;; ----------------------------------------------------------------------------
(defn message-command
  [t msg]
  {:type 4 :time t :msg msg})

(defmethod readbytes 4
  [cmd-type ois]
  (let [t (.readLong ois)
        len (.readInt ois)
        bytebuffer (byte-array len)
        bytes-read (.read ois bytebuffer 0 len)
        ; msg (apply str (map char bytebuffer))
        msg (IOUtils/toString bytebuffer)]
    (message-command t msg)))

(defmethod writebytes 4
  [cmd oos])

(defmethod printcmd 4
  [cmd ^PrintWriter out]
  (let [t (:time cmd)
        msg (:msg cmd)]
    (.print out msg)))

;; ----------------------------------------------------------------------------
;; Number Data
;; ----------------------------------------------------------------------------
(defn number-data-command
  ([n v] {:type 9 :name n :value v})
  ([] (number-data-command nil 0)))

(defmethod readbytes 9
  [cmd-type ois]
  (let [n (.readUTF ois)
        v (.readObject ois)]
    (number-data-command n v)))

(defmethod writebytes 9
  [cmd oos]
  (let [n (if-some [maybe-n (:name cmd)] maybe-n "")
        v (:value cmd)]
    (do
      (.writeByte oos (byte (:type cmd)))
      (.writeUTF oos n)
      (.writeObject oos v)
      (.flush oos))))

(defmethod printcmd 9
  [cmd ^PrintWriter out]
  (let [n (:name cmd)
        v (:value cmd)]
    (do
      (when-some [maybe-n n]
        (.print out maybe-n)
        (.print out " = "))
      (.println out v))))


;; ----------------------------------------------------------------------------
;; Okay
;; ----------------------------------------------------------------------------
(defn okay-command
  []
  {:type 6})

(defmethod readbytes 6
  [cmd-type ois]
  (okay-command))

(defmethod writebytes 6
  [cmd oos])

(defmethod printcmd 6
  [cmd ^PrintWriter out])


;; ----------------------------------------------------------------------------
;; Error
;; ----------------------------------------------------------------------------
(defn error-command
  [cause]
  {:type 0 :cause cause})

(defmethod readbytes 0
  [cmd ois]
  (let [cause (.readObject ois)]
    (error-command cause)))

(defmethod writebytes 0
  [cmd oos]
  (let [cause (:cause cmd)]
    (.writeByte oos (byte (:type cmd)))
    (.writeObject oos cause)
    (.flush oos)))


;; ----------------------------------------------------------------------------
;; Exit
;; ----------------------------------------------------------------------------
(defn exit-command
  ([c] (:type 2 :exit-code c))
  ([] (exit-command 0)))

(defmethod readbytes 2
  [cmd ois]
  (let [exit-code (.readInt ois)]
    (exit-command exit-code)))

(defmethod writebytes 2
  [cmd oos]
  (do
    (.writeByte oos (byte (:type cmd)))
    (.writeInt oos (:exit-command cmd))
    (.flush oos)))


;; ----------------------------------------------------------------------------
;; DEFAULT
;; ----------------------------------------------------------------------------
(defmethod readbytes :default
  [cmd-type ois]
  {:type cmd-type})


(defn exit-command
  [bytecode]
  {:type 2})

