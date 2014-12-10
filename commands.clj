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


(defonce int->cmd
  {3 (InstrumentCommand. nil)
   4 (MessageCommand.)})


(defprotocol CommandIO
  (readbytes [this ois])
  (writebytes [this oos]))


(defrecord InstrumentCommand [code]
  CommandIO
  (writebytes [this oos]
    (let [bytecode (:code this)]
      (do
        (.writeByte oos (byte 3))
        (.writeInt oos (count bytecode))
        (.write oos bytecode)
        (.writeInt oos (byte 0))
        (.flush oos))))
  (readbytes [this ois]))


(defrecord MessageCommand []
  CommandIO
  (readbytes [this ois])
  (writebytes [this oos]))
