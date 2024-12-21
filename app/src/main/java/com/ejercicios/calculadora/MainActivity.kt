package com.ejercicios.calculadora

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    /**********************************************************************************************/
    /** Esta variable la utilizaremos para acceder al TextView, tiene el mismo nombre            **/
    /**********************************************************************************************/
    var tvRes:TextView?=null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvRes=findViewById(R.id.tvRes) //Le asignamos a nuestra variable, la variable del textView del diseño, por medio del id
    }

    /**********************************************************************************************/
    /** Esta función es la que controla la funcionalidad, llama a otras funciones para obtener   **/
    /** el resultado de la operacion que se este realizando, llama a la funsión eval, la cual    **/
    /** se encarga de realizar la operaciones indicadas en la cadena que le enviamos.            **/
    /** Esta funcion es invocada al presionar cada uno de los botones, obtiene el texto de cada  **/
    /** boton y lo va concatenando en una cadena y finalmente le envia esa cadena a la función   **/
    /** eval para que retorne el resultado.                                                      **/
    /**********************************************************************************************/

    fun calcular(view : View){
        var boton=view as Button //recibimos el objeto de tipo button
        var textoBoton=boton.text.toString() //obtenemos el texto del boton que la esta llamando
        var concatenar=tvRes?.text.toString()+textoBoton //cada vez que se presiona un boton se va concatenando todo
        var concatenarSinCeros=quitarCerosIzquirda(concatenar) //quitamos los ceros de la izquierda
        if(textoBoton=="="){ //si presiona el signo = entonces mostramos el resultado
            var resultado=0.0
            try {
                resultado=eval(tvRes?.text.toString()) //enviamos la cadena concatenada a la funcion eval y obtenemos el resultado
                tvRes?.text=resultado.toString()
            }catch (e:Exception){
                tvRes?.text="ERROR"
            }
        }else if(textoBoton=="C"){ //si el usuario presiona la tecle C, entonces borramos la pantalla y mostramos el "0"
            tvRes?.text="0"
        }else{
            tvRes?.text=concatenarSinCeros
        }
    }

    /**********************************************************************************************/
    /** Esta funcion lo que hace es quitar los ceros del lado izquierdo                          **/
    /**********************************************************************************************/

    fun quitarCerosIzquirda(str : String):String{
        var i=0
        while (i<str.length && str[i]=='0')i++
        val sb=StringBuffer(str)
        sb.replace(0,i,"")
        return sb.toString()
    }

    /**********************************************************************************************/
    /** Esta es la función mas importante ya que recibe la cadena y nos devuelve el resultado    **/
    /**********************************************************************************************/

    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0
            fun nextChar() {
                ch = if (++pos < str.length) str[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt())) x += parseTerm() // addition
                    else if (eat('-'.toInt())) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('x'.toInt())) x *= parseFactor() // multiplication
                    else if (eat('/'.toInt())) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor() // unary plus
                if (eat('-'.toInt())) return -parseFactor() // unary minus
                var x: Double
                val startPos = pos
                if (eat('('.toInt())) { // parentheses
                    x = parseExpression()
                    eat(')'.toInt())
                } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
                    while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
                    while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
                    val func = str.substring(startPos, pos)
                    x = parseFactor()
                    x = if (func == "sqrt") Math.sqrt(x) else if (func == "sin") Math.sin(Math.toRadians(x)) else if (func == "cos") Math.cos(Math.toRadians(x)) else if (func == "tan") Math.tan(Math.toRadians(x)) else throw RuntimeException("Unknown function: $func")
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // exponentiation
                return x
            }
        }.parse()
    }

}