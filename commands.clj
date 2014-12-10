(defprotocol CommandIO
  (readbytes [this])
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
  (readbytes [this]))


(defrecord MessageCommand []
  CommandIO
  (readbytes [this])
  (writebytes [this oos]))
