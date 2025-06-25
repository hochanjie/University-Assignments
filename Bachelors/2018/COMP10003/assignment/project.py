# Name: Chan Jie Ho
# ID: 961948

from graphics import *
import csv

# make contents of table.py accessible as a list \
# as well as what each term would represent as a dictionary (in glossary.py)
table = open("table.py")
contents = [line for line in csv.reader(table)]
table.close()

glossary = open("glossary.py")
meanings = eval(glossary.read())
glossary.close()

# create a function that would allow the user to choose between 3 options \
# for each attribute for each part present until the user confirms their \
# choice, after which the function will return the chosen option
def confirm_attribute(part, attribute):
    current_part = image.copy() + []
    confirmation = "no"

    # create a loop that keeps running until the user confirms their choice
    while confirmation == "no":
        
        # create a copy of how the cartoon looks like up to the point of the \
        # program that the user is in as to avoid changing the whole cartoon
        current_attribute = image.copy() + []

        # give the user some instructions
        option = (input("\nChoose your {} {}! (0-2) ".format(part, attribute)))

        # allow for accidental mistyping as to avoid the program crashing
        while (option.isdigit() == False) or (int(option) not in range(0,3)):
            print("\nPlease choose an option within the range given!")
            option = (input("Choose your {} {}! (0-2) ".format(part, attribute)))
    
        # create a list of ONLY the options for the given part and attribute
        for line in contents:
            if (line[0] == part and line[1] == attribute):
                options = line[2:]

        chosen_option = meanings[options[int(option)]]

        # create a window that will allow the user to visualise the chosen option
        win = GraphWin("My Cartoon", 250, 250)

        if attribute == "shape": # selecting shape of head
            # add the chosen option to the copy of the cartoon so the user can see \
            # how the new feature will look together with whatever features already \
            # chosen and draw it out in the new window
            current_attribute.append(chosen_option)
            chosen_option.setFill("white")
        
            for obj in range(0, len(current_attribute)):
                current_attribute[obj].draw(win)
                win.getMouse() # pause to view the result
                win.close() # close window when done

            confirmation = input("\nIs this your {} {}? ".format(part, attribute)).lower()

            while (confirmation == "yes" or confirmation == "no") == False :
                print("\nPlease type either 'Yes' or 'No'!")
                confirmation = input("Is this your {} {}? ".format(part, attribute)).lower()   
    
            if confirmation == "yes":
                current_part = (current_attribute[obj])
                break
        
        else: # selecting colour of head
            current_part[-1].setFill(chosen_option)
            current_part[-1].draw(win)
            win.getMouse()
            win.close()

            confirmation = input("\nIs this your {} {}? ".format(part, attribute)).lower()

            # allow for case insensitivity
            while (confirmation == "yes" or confirmation == "no") == False :
                print("\nPlease type either 'Yes' or 'No'!")
                confirmation = input("Is this your {} {}? ".format(part, attribute)).lower()   
    
            if confirmation == "yes":
                current_part[-1].setFill(chosen_option)
                break
    
    return current_part;
    
# prototype will only include 2D feature
print("\nWelcome to the Online Cartoon Face Creator!")
redo = "yes"
while redo == "yes":
    image = []
        
    for part in ["head"]: # prototype will only include head
        attribute = iter(["shape", "colour"])
        image.append(confirm_attribute(part, next(attribute)))
        confirm_attribute(part, next(attribute))

    print("\nThis is how you look!")

    # create window to hold end result and draw it out
    win = GraphWin("My Cartoon", 250, 250)

    for obj in image:
        obj.draw(win)
    win.getMouse()
    win.close()

    redo = input("\nRe-do? ")
        
    while redo.lower() not in ["yes", "no"]:
        print("\nPlease type either 'Yes' or 'No'!")
        redo = input("Re-do? ")

print("\nCongratulations! You're done!!")
