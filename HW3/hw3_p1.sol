expr -> eor

# Or
eor -> eor OR econ
    | econ

# Concatenation
econ -> econ eprod
     | eprod

# STAR,PLUS operators
eprod -> word STAR
      | word PLUS
      | word

#A unit of terminal or another expression
word ->
     | LTR
     | EPS 
     | LPAR expr RPAR
