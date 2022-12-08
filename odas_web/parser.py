def helper(left, right, leftdiff, rightdiff):
    leftot = 0.0
    righttot = 0.0
    leftacc = 0.0
    rightacc = 0.0
    for i in range(len(left)):
        leftot = leftot+ left[i]
       # print("left is " + str(left[i]))
        righttot = righttot + right[i]
        leftacc = leftdiff[i] + leftacc
        rightacc = rightdiff[i] + rightacc
    rightavg = righttot/float(len(right))
    #rightaccavg = rightacc/float(len(rightdiff))
    #leftaccavg = leftacc/float(len(leftdiff))
    leftavg = leftot/float(len(left))
    #print("leftot is " + str(leftot))
    #print("leftavg is" + str(leftavg))
    #print("rightavg is" + str(rightavg))
    #print("leftaccavg is" + str(leftaccavg))
    #print("rightaccavg  is" + str(rightaccavg))
    if(rightavg >= 0.5 or leftavg >= 0.5):
        if rightavg >= 0.5 :
            if rightacc >= 0.0:
                print("danger")
                return
        if leftavg >= 0.5:
            if leftacc >= 0.00006:
                print("danger")
                return
    print("safe to cross")
    
        
            
def parsefile():
    f = open("outfile.txt",'r+')
    #print("starting")
    prev1 = 0.0
    prev2 = 0.0
    leftdiff = []
    rightdiff =[]
    left =[]
    right = []
    
    for i in f.readlines():
        ind1 = i.find(" ,")
        ind2 = i.find("left is")
        ##print("left is " +i[ind2+8:ind1])
        indhelp = i[ind2+8:ind1].find(".")
        numfloat =0.0
        numfloat2 = 0.0
        if indhelp  == -1:
            if i[ind2+8:ind1].strip() == "":
                continue
            ##print(i[ind2+8:ind1].strip())
            num = int(i[ind2+8:ind1].strip())
            numfloat = float(num)
        else:
            numfloat = float(i[ind2+8:ind1].strip())
        ##print(float(i[ind2+8:ind1]))
        leftdiff.append(numfloat - prev1)
        left.append(numfloat)
        prev1 = numfloat
        ind3 = i.find("right is")
        indhelp2 = i[ind3+9:].strip().find(".")
        if indhelp2 == -1:
            num2 = int(i[ind3+9:len(i) -1].strip())
            numfloat2 = float(num2)
        else:     
            numfloat2 = float(i[ind3+9:len(i) -1].strip())
        ##print(float(i[ind3+9:]))
        ##print("right is")
        ##print(numfloat2)
        rightdiff.append(numfloat2 -prev2)
        right.append(numfloat2)
        prev2 = numfloat2
    #print("left followed by left diff \n")
    #print(left[len(left)-1001:])
    #print("leftdiff")
    #print(leftdiff[len(leftdiff)-1001:])
    #print("right followed by right diff \n")
    #print(right[len(right) -101:])
    #print("rightdiff")
    #print(rightdiff[len(rightdiff)-101:])
    helper(left[len(left)-1001:],right[len(right) -1001:], leftdiff[len(leftdiff)-1001:], rightdiff[len(rightdiff)-1001:])

parsefile()


        
    
