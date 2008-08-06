/* sos/lambda4.c */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "sos.h"
#include "internal.h"
#include "primitive.h"

/*--------------------------------------------------------------------------*/

Static_Declarations ("lambda4", 4, 14, 3);

#include "primitive.i.c"

/*--------------------------------------------------------------------------*/

SoS_primitive_result* sos_lambda4 (int i, int j, int k, int l)
     /* Returns significant term of Lambda4 epsilon-determinant.
        Assumes indices in proper range, pairwise different, and sorted. */
{
#ifdef __DEBUG__
  if (sos_proto_e_flag)
    {
      lia_clear ();
      print ("sos_lambda4 (%d,%d,%d,%d)", i, j, k, l);
      print (" (");
      print ("%s,%s,%s,1;", Pi(i,1), Pi(i,2), Pi(i,3));
      print ("%s,%s,%s,1;", Pi(j,1), Pi(j,2), Pi(j,3));
      print ("%s,%s,%s,1;", Pi(k,1), Pi(k,2), Pi(k,3));
      print ("%s,%s,%s,1;", Pi(l,1), Pi(l,2), Pi(l,3));
      print (")\n");
    }
#endif
/* C code generated by 'ccode' from 'gee' file "Lambda4.out" */
Initialize ();
Epsilon_Term (0);
Positive_Coefficient (Minor4 (i, j, k, l, 1, 2, 3, 0));
Epsilon_Term (1);
Epsilon (i,3);
Positive_Coefficient (Minor3 (j, k, l, 1, 2, 0));
Epsilon_Term (2);
Epsilon (i,2);
Negative_Coefficient (Minor3 (j, k, l, 1, 3, 0));
Epsilon_Term (3);
Epsilon (i,1);
Positive_Coefficient (Minor3 (j, k, l, 2, 3, 0));
Epsilon_Term (4);
Epsilon (j,3);
Negative_Coefficient (Minor3 (i, k, l, 1, 2, 0));
Epsilon_Term (5);
Epsilon (j,3);
Epsilon (i,2);
Positive_Coefficient (Minor2 (k, l, 1, 0));
Epsilon_Term (6);
Epsilon (j,3);
Epsilon (i,1);
Negative_Coefficient (Minor2 (k, l, 2, 0));
Epsilon_Term (7);
Epsilon (j,2);
Positive_Coefficient (Minor3 (i, k, l, 1, 3, 0));
Epsilon_Term (8);
Epsilon (j,2);
Epsilon (i,1);
Positive_Coefficient (Minor2 (k, l, 3, 0));
Epsilon_Term (9);
Epsilon (j,1);
Negative_Coefficient (Minor3 (i, k, l, 2, 3, 0));
Epsilon_Term (10);
Epsilon (k,3);
Positive_Coefficient (Minor3 (i, j, l, 1, 2, 0));
Epsilon_Term (11);
Epsilon (k,3);
Epsilon (i,2);
Negative_Coefficient (Minor2 (j, l, 1, 0));
Epsilon_Term (12);
Epsilon (k,3);
Epsilon (i,1);
Positive_Coefficient (Minor2 (j, l, 2, 0));
Epsilon_Term (13);
Epsilon (k,3);
Epsilon (j,2);
Positive_Coefficient (Minor2 (i, l, 1, 0));
Epsilon_Term (14);
Epsilon (k,3);
Epsilon (j,2);
Epsilon (i,1);
Coefficient (Integer (1));
Finish ();
}